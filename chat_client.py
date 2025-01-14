# pip install websocket-client
import json
from datetime import datetime
from sys import stdout
from threading import Thread
from typing import Any, Callable, Optional, Self, cast

from websocket import WebSocket, WebSocketApp


class KeyboardThread(Thread):
    callback: Optional[Callable[[str], None]]

    def __init__(
            self: Self,
            callback: Optional[Callable[[str], None]] = None,
            name: str = 'keyboard-input-thread') -> None:
        super(KeyboardThread, self).__init__(name=name, daemon=True)
        self.callback = callback

    def run(self: Self) -> None:
        while True:
            if self.callback is not None:
                self.callback(input())


class Room:
    id: str
    name: str

    def __init__(self: Self, **kwargs: Any) -> None:
        self.id = kwargs["roomId"]
        self.name = kwargs["name"]


class User:
    session_id: str
    name: str

    def __init__(self: Self, **kwargs: Any) -> None:
        self.session_id = kwargs["sessionId"]
        self.name = kwargs["username"] \
            if "username" in kwargs else kwargs["name"]


class ChatApplicationClient:
    address: str
    socket_app: WebSocketApp
    keyboard_thread: KeyboardThread

    auth: bool
    session_id: str
    username: str
    joined_time: datetime
    socket: Optional[WebSocket]
    status: Optional[str]
    rooms: list[Room]
    current_room: Optional[Room]

    def __init__(self: Self, address: str) -> None:
        self.address = address
        self.socket_app = WebSocketApp(
            self.address,
            on_open=self.on_open,
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close)
        self.keyboard_thread = KeyboardThread(self.continue_input)
        self.auth = False
        self.session_id = ""
        self.username = ""
        self.socket = None
        self.status = None
        self.rooms = []
        self.current_room = None

    def continue_input(self: Self, value: str):
        if self.socket is None:
            return
        if self.status == "Auth#createUser":
            if value.strip() == "":
                print("Username cannot be empty.")
                stdout.write("Input your name>> ")
                stdout.flush()
                return
            request = json.dumps({
                "kind": "Auth#createUser",
                "sessionId": self.session_id,
                "username": value})
            self.socket.send_text(request)
            return
        if not value.startswith("/"):
            if self.current_room is not None:
                if value.strip() == "":
                    return
                request = json.dumps({
                    "kind": "Room#sendChat",
                    "sessionId": self.session_id,
                    "roomId": self.current_room.id,
                    "content": value})
                self.socket.send_text(request)
                return
            print("Unknown command requested. Try again.")
            return
        splits = value[1:].split(maxsplit=2)
        if len(splits) < 2:
            command, = splits
            argument = ""
        else:
            command, argument = value[1:].split(maxsplit=2)
        if command == "fetch":
            request = json.dumps({
                "kind": "Room#fetchRooms",
                "sessionId": self.session_id})
            self.socket.send_text(request)
            return
        if command == "create":
            if argument.strip() == "":
                print("Room name cannot be empty.")
                return
            request = json.dumps({
                "kind": "Room#createRoom",
                "sessionId": self.session_id,
                "name": argument})
            self.socket.send_text(request)
            return
        if command == "join":
            if argument.strip() == "":
                print("Room name cannot be empty.")
                return
            rooms = [r for r in self.rooms if r.name == argument]
            if len(rooms) == 0:
                print(f"Cannot join room \"{argument}\"")
                return
            room = rooms[0]
            request = json.dumps({
                "kind": "Room#join",
                "sessionId": self.session_id,
                "roomId": room.id})
            self.socket.send_text(request)
            return
        if command == "leave":
            if self.current_room is None:
                print("You don't have joined any rooms")
                return
            request = json.dumps({
                "kind": "Room#leave",
                "sessionId": self.session_id,
                "roomId": self.current_room.id})
            self.socket.send_text(request)
            return
        if command == "exit":
            self.socket.close()

    def on_open(self: Self, socket: WebSocket) -> None:
        self.socket = socket

    def on_message(self: Self, socket: WebSocket, message: str) -> None:
        data = cast(dict, json.loads(message))
        kind = cast(str, data.get("kind"))
        self.status = kind
        if kind == "Auth#createUser":
            if self.auth:
                return
            self.session_id = cast(str, data["sessionId"])
            self.status = kind
            stdout.write("Input your name>> ")
            stdout.flush()
        elif kind == "Auth#authUser":
            self.auth = True
            self.joined_time = datetime.fromisoformat(data["joinedTime"])
            request = json.dumps({
                "kind": "Room#fetchRooms",
                "sessionId": self.session_id})
            socket.send_text(request)
        elif kind == "Room#fetchRooms":
            self.rooms = [Room(**args) for args in data["rooms"]]
            if len(self.rooms) == 0:
                print("No room is open. Create a new room and join it.")
            else:
                print("Here's rooms open:")
                for i, room in enumerate(self.rooms):
                    print(f"{i + 1}. {room.name}")
        elif kind == "Room#createRoom":
            room = Room(**data)
            self.rooms.insert(0, room)
            request = json.dumps({
                "kind": "Room#join",
                "sessionId": self.session_id,
                "roomId": room.id})
            socket.send_text(request)
        elif kind == "Room#join":
            room = [r for r in self.rooms if r.id == data["roomId"]][0]
            user = User(**data["user"])
            self.current_room = room
            print(f"\"{user.name}\" has joined \"{room.name}\"")
        elif kind == "Room#leave":
            user = User(**data["user"])
            if user.session_id == self.session_id:
                self.current_room = None
                print("You have left the room.")
            else:
                print(f"\"{user.name}\" has left the room.")
        elif kind == "Room#sendChat":
            timestamp = datetime.fromisoformat(data["timestamp"])
            user = User(**data["user"])
            content = data["content"]
            print(f"{user.name}<{timestamp}>: {content}")

    def on_error(self: Self, socket: WebSocket, error: Any) -> None:
        print(error)

    def on_close(
            self: Self,
            socket: WebSocket,
            status_code: Optional[Any],
            message: Optional[Any]) -> None:
        return

    def start(self: Self) -> None:
        self.keyboard_thread.start()
        self.socket_app.run_forever()


ADDRESS = "ws://localhost:8080/chat"


def main():
    client = ChatApplicationClient(ADDRESS)
    client.start()


if __name__ == "__main__":
    main()
