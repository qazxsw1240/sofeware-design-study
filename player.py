from __future__ import annotations

import sys
from abc import ABC, abstractmethod
from dataclasses import dataclass
from queue import Queue
from typing import Optional, Self, TextIO, override
from uuid import UUID, uuid4


@dataclass
class Region:
    uuid: UUID
    name: str


@dataclass
class Item:
    name: str


class SystemNotifier:
    stream: TextIO

    def __init__(self: Self, stream: TextIO = sys.stdout) -> None:
        self.stream = stream

    def notify(self: Self, message: str) -> None:
        self.stream.write(message)
        self.stream.write("\n")


class SystemNotifierCollection:
    queue: Queue[str]

    def __init__(self: Self, capacity: int = 100) -> None:
        self.queue = Queue(capacity)

    def collect(self: Self, message: str) -> None:
        self.queue.put(message, block=False)


class Observer(ABC):
    pass


class PlayerJoinRegionObserver(Observer):
    @abstractmethod
    def player_join(self: Self, player: Player, region: Region):
        pass


class PlayerMoveRegionObserver(Observer):
    @abstractmethod
    def player_move(
            self: Self,
            player: Player,
            old_region: Region,
            new_region: Region):
        pass


class Player:
    name: str
    notifier: SystemNotifier
    notifier_collection: SystemNotifierCollection
    region: Optional[Region]
    items: list[Item]
    observers: list[Observer]

    def __init__(
            self: Self,
            name: str,
            notifier: SystemNotifier,
            notifier_collection: SystemNotifierCollection) -> None:
        self.name = name
        self.notifier = notifier
        self.notifier_collection = notifier_collection
        self.region = None
        self.items = []
        self.observers = []

    def add_observer(self: Self, observer: Observer) -> None:
        self.observers.append(observer)

    def remove_observer(self: Self, observer: Observer) -> None:
        self.observers.remove(observer)

    def get_observers[L](self: Self, type: type[L]) -> list[L]:
        observers = []
        for observer in self.observers:
            if isinstance(observer, type):
                observers.append(observer)
        return observers

    def join(self: Self, region: Region) -> bool:
        if self.region == region:
            return False
        if self.region is None:
            observers = self.get_observers(PlayerJoinRegionObserver)
            for observer in observers:
                observer.player_join(self, region)
        elif self.region != region:
            observers = self.get_observers(PlayerMoveRegionObserver)
            for observer in observers:
                observer.player_move(self, self.region, region)
        self.region = region
        return True

    def leave(self: Self) -> bool:
        if self.region is None:
            return False
        region = self.region
        message = f"Player {self.name} has leaved region {region.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)
        self.region = None
        return True

    def add_item(self: Self, item: Item) -> None:
        self.items.append(item)
        message = f"Player {self.name} has acquired item {item.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)

    def release_item(self: Self, item: Item) -> None:
        if item not in self.items:
            return
        self.items.remove(item)
        message = f"Player {self.name} has released item {item.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)


REGION_KR = Region(uuid4(), "South Korea")
REGION_JP = Region(uuid4(), "Japan")
REGION_CN = Region(uuid4(), "China")


class PlayerEventNotifier(PlayerJoinRegionObserver, PlayerMoveRegionObserver):
    notifier: SystemNotifier
    notifier_collection: SystemNotifierCollection

    def __init__(
            self: Self,
            notifier: SystemNotifier,
            notifier_collection: SystemNotifierCollection) -> None:
        super().__init__()
        self.notifier = notifier
        self.notifier_collection = notifier_collection

    @override
    def player_join(self: Self, player: Player, region: Region):
        message = f"Player {player.name} has joined region {region.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)

    @override
    def player_move(
            self: Self,
            player: Player,
            old_region:
            Region, new_region: Region):
        message = f"Player {player.name} has moved to region {new_region.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)


class PlayerEventItemAddObserver(PlayerJoinRegionObserver):
    def __init__(self: Self) -> None:
        super().__init__()

    @override
    def player_join(self: Self, player: Player, region: Region):
        player.add_item(Item("접속 보상"))
        for item in player.items:
            print(item)


def main():
    system_notifier = SystemNotifier()
    notifier_collection = SystemNotifierCollection()
    event_notifier = PlayerEventNotifier(system_notifier, notifier_collection)
    player = Player("admin", system_notifier, notifier_collection)
    player.add_observer(event_notifier)
    player.add_observer(PlayerEventItemAddObserver())
    player.join(REGION_KR)
    player.join(REGION_JP)
    player.add_item(Item("Sword"))
    player.release_item(Item("Sword"))
    player.join(REGION_CN)
    player.leave()


if __name__ == "__main__":
    main()
