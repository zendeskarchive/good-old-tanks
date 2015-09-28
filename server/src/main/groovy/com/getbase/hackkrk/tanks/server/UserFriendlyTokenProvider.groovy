package com.getbase.hackkrk.tanks.server

import static com.getbase.hackkrk.tanks.server.util.CollectionUtils.random

class UserFriendlyTokenProvider {

    private final List<List<String>> sources

    UserFriendlyTokenProvider(String... sources) {
        this.sources = sources.collect { s ->
            UserFriendlyTokenProvider.getResourceAsStream("/tokens/${s}.txt").readLines()
        }
    }

    String generate() {
        sources.collect { s -> random(s).get() }.join('')
    }

    List<String> generate(int count) {
        (1..count).collect { generate() }
    }


}
