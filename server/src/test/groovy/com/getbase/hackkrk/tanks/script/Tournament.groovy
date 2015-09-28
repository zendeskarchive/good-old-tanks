package com.getbase.hackkrk.tanks.script

import groovy.util.logging.Slf4j
import groovyx.net.http.RESTClient

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

import static groovyx.net.http.ContentType.JSON

@Slf4j
class Tournament {
    def host = "localhost"
    def port = "9999"

    def id
    def adminToken

    def static create(String name) {
        def t = client().post(
                path: "tournaments/$name"
        )
        log.info "Created tournament $t.data.id with token $t.data.adminToken"
        def tokens = t.data.userTokens.collect({ "\t$it" }).join('\n')
        log.info "User access tokens: \n\t--------------------------------\n$tokens\n\t--------------------------------"
        new Tournament(id: t.data.id, adminToken: t.data.adminToken)
    }

    def start() {
        def r = client().post(
                path: "tournaments/${id}/start",
                headers: [
                        Authorization: adminToken
                ],
        )
        log.info "Started tournament with configuration: $r.data.configuration"
        r
    }

    def set(Map params) {
        def r = client().post(
                path: "tournaments/${id}/configuration",
                headers: [
                        Authorization: adminToken
                ],
                body: params
        )
        log.info "Updated configuration with $params. Current config $r.data"
        r
    }

    def config() {
        def configuration = client().get(
                path: "tournaments/$id/configuration",
                headers: [
                        Authorization: adminToken
                ],
        ).data
        log.info "Configuration: $configuration"
        configuration
    }

    def at(String time) {
        def t = LocalTime.parse(time)
        def ldt =  t.atDate(LocalDate.now())
        new Scheduler(time: ldt, target: this)
    }

    def after(Map params) {
        new AfterScheduler(target: this, seconds: params['seconds'])
    }

    def RESTClient client() {
        new RESTClient("http://$host:$port/", JSON)
    }
}

@Slf4j
class AfterScheduler {
    long seconds
    Object target

    def methodMissing(String name, args) {
        new Timer().schedule(
                { target.invokeMethod(name, args) } as TimerTask,
                seconds * 1000L
        )
        log.info "Scheduled $name $args in $seconds seconds"
    }
}

@Slf4j
class Scheduler {
    LocalDateTime time
    Object target

    def methodMissing(String name, args) {
        if (time.isBefore(LocalDateTime.now())) {
            log.info("Skipping outdated '$name $args': $time")
        } else {
            def date = Date.from(time.atZone(ZoneId.systemDefault()).toInstant())
            new Timer().schedule(
                    { target.invokeMethod(name, args) } as TimerTask,
                    date
            )
            log.info "Scheduled '$name $args' for $time"
        }
    }
}
