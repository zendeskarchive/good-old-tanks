package com.getbase.hackkrk.tanks.server

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import com.getbase.hackkrk.tanks.server.simulation.utils.Point
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import static org.springframework.boot.SpringApplication.run

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
class Application {

    static void main(String[] args) {
        run Application, args
    }

    @Bean
    ObjectMapper objectMapper() {
        new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JSR310Module())
                .enable(INDENT_OUTPUT)
    }

    @Bean
    UserFriendlyTokenProvider tokenProvider() {
        new UserFriendlyTokenProvider(
                'traits',
                'colors',
                'animals',
                'animals',
        )
    }

    @Bean
    TaskScheduler scheduler() {
        new ThreadPoolTaskScheduler(
                // FIXME extract to application.yml
                poolSize: 20
        )
    }

    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer(@Value('${http.async.timeout:30}') long asyncTimeout) {
        { TomcatEmbeddedServletContainerFactory f ->
            f.addConnectorCustomizers({
                it.asyncTimeout = asyncTimeout * 1000
            } as TomcatConnectorCustomizer)
        }
    }

    @Bean
    CommandLineRunner tournamentLoader(ApplicationContext applicationContext, TournamentRepository repository) {
        { String... args ->
            repository
                    .loadAll()
                    .each { t ->
                applicationContext
                        .getBean(TournamentStateMachine)
                        .recover(t)
            }
        }
    }
    
    @Bean
    public Module customDoubleSerialization() {
        SimpleModule module = new SimpleModule("MyDoubleModule", new Version(1, 0, 0, null));
        module.addSerializer(Point.class, new CustomPointSerializer());
        return module;
    }

}
