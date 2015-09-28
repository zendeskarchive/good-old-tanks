# Good Old Tanks - fronted application for HackKRK

## Requirements

  * nodejs
  * npm
  * gulp: _npm install -g gulp_

### for test server:
  
  * python 2.*
  * virtualenv

## Build & run

  * download dependencies: _npm install_
  * build: _npm run build_
  * run dev server: _npm run serve_
  * build and run dev server: _npm run all_
  * [http://localhost:8080/](http://localhost:8080/)
  * __IMPORTANT:__ __js/config.coffee__, __launcher.html__ and __ranking.html__ files have hardcoded backend server address and port, if backend server is configured on custom endpoint (default is localhost:9999) then __endpoint__ variable must be configured accordingly
  
## Run test server

  * create virtualenv:  _virtualenv ./ui/server/env_
  * activate virtualenv: source ./ui/server/env/bin/activate
  * install bottle framework: _pip install bottle_
  * run from ui/server/src folder: python server.py

## Copyrights

  * Graphics from Phaser [turorial](http://phaser.io/tutorials/coding-tips-002) (Amiga Tanx Copyright 1991 Gary Roberts)


