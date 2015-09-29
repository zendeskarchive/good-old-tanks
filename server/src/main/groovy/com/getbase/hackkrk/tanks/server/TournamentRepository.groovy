package com.getbase.hackkrk.tanks.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.getbase.hackkrk.tanks.server.controller.NotFoundException
import com.getbase.hackkrk.tanks.server.controller.UnauthorizedException
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.StopWatch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executor
import java.util.function.Consumer

import static com.google.common.base.Throwables.getStackTraceAsString
import static com.google.common.util.concurrent.MoreExecutors.directExecutor
import static java.time.Instant.now
import static java.util.concurrent.Executors.newSingleThreadExecutor

@Slf4j
@Component
class TournamentRepository {

    private final ConcurrentMap<String, Tournament> tournaments = new ConcurrentHashMap<>()

    private final ObjectMapper objectMapper

    private final File storageLocation

    private final Executor persister

    private final boolean backupEnabled

    private final int tournamentLimit

    private final boolean testLoadEnabled

    private final boolean testLoadFailFast

    @Autowired
    TournamentRepository(
            ObjectMapper objectMapper,
            @Value('${storage.location}') File storageLocation,
            @Value('${storage.save.enabled:true}') boolean saveEnabled,
            @Value('${storage.save.async:true}') boolean saveAsync,
            @Value('${storage.backup.enabled:true}') boolean backupEnabled,
            @Value('${tournament.limit:1}') int tournamentLimit,
            @Value('${storage.test.load.enabled:false}') boolean testLoadEnabled,
            @Value('${storage.test.load.fail.fast:false}') boolean testLoadFailFast) {
        this.objectMapper = objectMapper
        this.storageLocation = storageLocation
        this.persister = saveEnabled ?
                (saveAsync ? newSingleThreadExecutor({ r -> new Thread(r, 'persister') }) : directExecutor())
                : { r -> log.warn("Save of tournaments is DISABLED!") } as Executor
        this.backupEnabled = backupEnabled && saveEnabled
        this.tournamentLimit = tournamentLimit
        this.testLoadEnabled = testLoadEnabled
        this.testLoadFailFast = testLoadFailFast
    }

    Tournament create(Tournament tournament) {
        log.info "Creating tournament #{}", tournament.id
        if (tournaments.size() >= tournamentLimit) {
            throw new UnauthorizedException("Maximum number of tournaments reached!")
        }

        def previous = tournaments.putIfAbsent(tournament.id, tournament)
        if (previous != null) {
            throw new UnauthorizedException("Tournament #$tournament.id already exists!")
        }

        save(tournament)
    }

    Tournament save(Tournament tournament, boolean andBackup = false) {
        log.info "Scheduling tournament #{} saving", tournament.id
        if (!tournaments[tournament.id].is(tournament)) {
            throw new IllegalArgumentException("Tournament #$tournament.id exists in the repository but it is not the same instance as passed to the save() method!")
        }

        persister.execute { this.doSave(tournament, andBackup) }
        tournament
    }

    Tournament load(String tournamentId) {
        log.info "Loading tournament #{}", tournamentId
        create(deserializeTournament(getTournamentFile(tournamentId)))
    }

    List<Tournament> loadAll() {
        log.info "Loading all saved tournaments."
        this.storageLocation
                .listFiles()
                .findAll { f -> f.isFile() && f.name.endsWith('.json') }
                .collect { it.name - '.json' }
                .collect { load it }
    }

    Tournament withTournament(String id, boolean andBackup, @DelegatesTo(Tournament) Closure<?> action) {
        def tournament = getById(id)
        tournament.with(action)
        save(tournament, andBackup)
    }

    Tournament withTournament(String id, @DelegatesTo(Tournament) Closure<?> action) {
        withTournament(id, false, action)
    }

    Tournament withTournament(String id, Consumer<Tournament> consumer) {
        withTournament(id) {
            consumer.accept(delegate)
        }
    }

    Tournament getById(String tournamentId) {
        Optional
                .ofNullable(tournaments[tournamentId])
                .orElseThrow { new NotFoundException("Tournament #$tournamentId does not exist!") }
    }

    Collection<Tournament> list() {
        tournaments.values()
    }

    private void doSave(Tournament tournament, boolean andBackup) {
        timed("Saving tournament #${tournament.id}") {
            def serializedTournament
            synchronized (tournament) {
                log.info "Saving tournament #{}", tournament.id
                serializedTournament = serializeTournament(tournament)
                getTournamentFile(tournament.id).text = serializedTournament
                if (andBackup) {
                    backup(tournament, serializedTournament)
                }
            }
            if (testLoadEnabled) {
                testLoad(tournament.id, serializedTournament)
            }
        }

        tournament
    }

    private backup(Tournament tournament, String serialized) {
        if (!backupEnabled) {
            log.warn "Backup is DISABLED!"
            return
        }

        if (!tournament.state.backupable) {
            return
        }

        log.info "Backing up tournament #{}", tournament.id
        getTournamentFile(tournament.id, 'backup', ".${now().toEpochMilli()}.${tournament.state.class.simpleName}").text = serialized
    }

    private String serializeTournament(Tournament tournament) {
        objectMapper.writeValueAsString(tournament)
    }

    private Tournament deserializeTournament(source) {
        objectMapper.readValue(source, Tournament)
    }

    private File getTournamentFile(String tournamentId, String directory = '.', String suffix = '') {
        def dir = new File(storageLocation, directory)
        dir.mkdirs()
        new File(dir, "${tournamentId}.json$suffix")
    }

    private static void timed(String name, Runnable r) {
        def watch = new StopWatch()
        watch.start()
        r.run()
        watch.stop()
        log.info "{} took {}ms", name, watch.time
    }

    // TEST LOAD
    private void testLoad(String id, String serializedTournament) {
        log.info "Test loading tournament #{}", id
        def now = new Date().time
        try {
            def tournament = deserializeTournament(serializedTournament)
            def serializedAgain = serializeTournament(tournament)
            if (serializedTournament == serializedAgain) {
                return
            }

            def failedDir = new File(storageLocation, 'failed')
            failedDir.mkdirs()
            new File(failedDir, "${id}-${now}-original.json").text = serializedTournament
            new File(failedDir, "${id}-${now}-2ndpass.json").text = serializedAgain
            log.warn "Original JSON and 2nd pass JSON does not match for tournament #{}", id
        } catch (Exception e) {
            def failedDir = new File(storageLocation, 'failed')
            failedDir.mkdirs()
            new File(failedDir, "${id}-${now}.json").text = serializedTournament
            new File(failedDir, "${id}-${now}-exception.txt").text = getStackTraceAsString(e)
            log.warn "Test loading tournament #{} failed!", id
        }

        if (testLoadFailFast) {
            throw new RuntimeException("TEST LOAD FAILED for tournament #${id}. BYE! BYE!")
        }
    }

}
