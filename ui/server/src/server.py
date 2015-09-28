from bottle import route, run, hook, response
import json

data = None
with open('tournament.json') as f:
    data = json.loads(f.read())

@hook('after_request')
def enable_cors():
    response.headers['Access-Control-Allow-Origin'] = '*'

@route('/tournaments')
def tournament():
    return data

if __name__ == '__main__':
    run(host='localhost', port=9988)
