package com.getbase.hackkrk.tanks.server

import spock.lang.Specification

class UserFriendlyTokenProviderSpec extends Specification {

    def "should generate cool names"() {
        given:
        def provider = new UserFriendlyTokenProvider('traits', 'colors', 'animals', 'animals')

        expect:
        100_000.times {
            assert provider.generate() ==~ /([A-Z][a-z]+){4,}/
        }
    }

}
