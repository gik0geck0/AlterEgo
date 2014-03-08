#!/usr/bin/env python3

from bottle import run, template, post, get, put

# Create a new server
@post('/server/new')
def new_server():
    return 'Creating a new server'

# Show simple info about the server
@get('/server/<serverid:int>')
def get_server(serverid):
    return template('Server: {{serverid}}', serverid=serverid)

# Update the presence of a user
@post('/server/<serverid:int>')
def user_entrance(serverid):
    return template('User joined as 0')

# Fetch global info about a character
@get('/server/<serverid:int>/<userid:int>/')
def get_user(serverid, userid):
    return template('Server {{serverid}}, getting User Id {{userid}}', serverid=serverid, userid=userid)

# Update global info about a character
@put('/server/<serverid:int>/<userid:int>/')
def update_user(serverid, userid):
    return template('Server {{serverid}}, updating User Id {{userid}}', serverid=serverid, userid=userid)

# Change something on the map
@put('/server/<serverid:int>/map')
def update_map(serverid):
    return template('Server {{serverid}} is updating the map', serverid=serverid)

# Tell me about everything on the map
@get('/server/<id:int>/map')
def get_map(serverid):
    return template('Server {{serverid}}, getting the map', serverid=serverid)

# Give me the entire chat history
@get('/server/<serverid:int>/chat')
def get_chat(serverid):
    return template('Server {{serverid}} getting the chat history', serverid=serverid)

# Someone has said something
@post('/server/<serverid:int>/chat')
def add_chat(serverid):
    return template('Server {{serverid}} sending chat message', serverid=serverid)

if __name__ == "__main__":
    run(host='localhost', port=63230)
