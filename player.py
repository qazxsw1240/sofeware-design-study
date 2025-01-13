import sys
from dataclasses import dataclass
from queue import Queue
from typing import Optional, Self, TextIO
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


class Player:
    name: str
    notifier: SystemNotifier
    notifier_collection: SystemNotifierCollection
    region: Optional[Region]
    items: list[Item]

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

    def join(self: Self, region: Region) -> bool:
        if self.region == region:
            return False
        if self.region is None:
            message = f"Player {self.name} has joined region {region.name}"
            self.notifier.notify(message)
            self.notifier_collection.collect(message)
        elif self.region != region:
            message = f"Player {self.name} has moved to region {region.name}"
            self.notifier.notify(message)
            self.notifier_collection.collect(message)
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
        if self.region is None:
            raise Exception("Player has not joined any regions")
        self.items.append(item)
        message = f"Player {self.name} has acquired item {item.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)

    def release_item(self: Self, item: Item) -> None:
        if self.region is None:
            raise Exception("Player has not joined any regions")
        if item not in self.items:
            return
        self.items.remove(item)
        message = f"Player {self.name} has released item {item.name}"
        self.notifier.notify(message)
        self.notifier_collection.collect(message)


REGION_KR = Region(uuid4(), "South Korea")
REGION_JP = Region(uuid4(), "Japan")
REGION_CN = Region(uuid4(), "China")


def main():
    system_notifier = SystemNotifier()
    notifier_collection = SystemNotifierCollection()
    player = Player("admin", system_notifier, notifier_collection)
    player.join(REGION_KR)
    player.join(REGION_JP)
    player.add_item(Item("Sword"))
    player.release_item(Item("Sword"))
    player.join(REGION_CN)
    player.leave()


if __name__ == "__main__":
    main()
