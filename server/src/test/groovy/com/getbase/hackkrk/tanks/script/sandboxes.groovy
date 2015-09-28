package com.getbase.hackkrk.tanks.script

startSandboxes()

def startSandboxes() {
    (1..5).each {
        new Tournament(id: "sandbox-$it", adminToken: 'RycqhOLbG6aqELYuhpJIyz6xmgLGDQMf', host: '10.12.200.239').start()
    }
}
