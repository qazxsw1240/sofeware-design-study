from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import IntEnum
from typing import Iterable, Iterator, Optional, Self, Sized, override


@dataclass
class Contact:
    """
    Represents a contact.
    """

    name: str
    phone_address: str


class ContactCollection(Sized, Iterable[Contact]):
    contacts: list[Contact]

    def __init__(self: Self, *contacts: Contact) -> None:
        self.contacts = list(contacts)

    @property
    def empty(self: Self) -> bool:
        return len(self.contacts) == 0

    def find(self: Self, name: str) -> Optional[Contact]:
        for contact in self.contacts:
            if contact.name == name:
                return contact
        return None

    def add(self: Self, contact: Contact) -> bool:
        if contact in self.contacts:
            return False
        self.contacts.append(contact)
        return True

    def remove(self: Self, contact: Contact) -> bool:
        if contact not in self.contacts:
            return False
        self.contacts.remove(contact)
        return True

    @override
    def __len__(self: Self) -> int:
        return len(self.contacts)

    @override
    def __iter__(self) -> Iterator[Contact]:
        return iter(self.contacts)


class ContactStrategyResult(IntEnum):
    CONTINUE = 0
    EXIT = 1


class ContactStrategy(ABC):
    @property
    @abstractmethod
    def description(self: Self) -> str:
        pass

    @abstractmethod
    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        pass


class ContactManagerApplication:
    contacts: ContactCollection
    strategies: list[ContactStrategy]

    def __init__(self: Self, *strategies: ContactStrategy) -> None:
        self.contacts = ContactCollection()
        self.strategies = list(strategies)

    def run(self: Self):
        result = ContactStrategyResult.CONTINUE
        while result != ContactStrategyResult.EXIT:
            command = self.fetch_command()
            strategy = self.strategies[command - 1]
            result = strategy.handle_contacts(self.contacts)

    def print_task_info(self: Self) -> None:
        print("------------------------------------------------")
        print("Input the number for the task which you want.")
        for i, strategy in enumerate(self.strategies):
            print(f"{i + 1}. {strategy.description}")

    def fetch_command(self: Self) -> int:
        self.print_task_info()
        command = int(input(">>"))
        while command < 1 or command > len(self.strategies):
            print("Invalid input found.")
            self.print_task_info()
            command = int(input(">>"))
        return command


class ContactPrintStrategy(ContactStrategy):
    @property
    @override
    def description(self: Self) -> str:
        return "Print Contacts"

    @override
    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        print(f"{self.description}:")
        if contacts.empty:
            print("\tNo contact is registered.")
        else:
            for contact in contacts:
                print(f"\t{contact}")
        return ContactStrategyResult.CONTINUE


class ContactAddStrategy(ContactStrategy):
    @property
    @override
    def description(self: Self) -> str:
        return "Add Contact"

    @override
    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        print(f"{self.description}:")
        print("\tInput the name for new contact:")
        name = input(">>")
        print("\tInput the phone address for new contact:")
        phone_address = input(">>")
        contact = Contact(name, phone_address)
        contacts.add(contact)
        return ContactStrategyResult.CONTINUE


class ContactRemoveStrategy(ContactStrategy):
    @property
    @override
    def description(self: Self) -> str:
        return "Remove Contact"

    @override
    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        print(f"{self.description}:")
        print("\tInput the name to remove contact:")
        name = input(">>")
        target_contact = None
        for contact in contacts:
            if contact.name == name:
                target_contact = contact
                break
        if target_contact is None:
            print(f'Cannot find any contacts with the name "{name}".')
        else:
            contacts.remove(target_contact)
            print("\tSuccessfully removed the contact.")
        return ContactStrategyResult.CONTINUE


class ContactUpdateStrategy(ContactStrategy):
    @property
    @override
    def description(self: Self) -> str:
        return "Update Contact"

    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        print(f"{self.description}:")
        print("\tInput the name to update contact:")
        name = input(">>")
        target_contact = None
        for contact in contacts:
            if contact.name == name:
                target_contact = contact
                break
        if target_contact is None:
            print(f'Cannot find any contacts with then name "{name}".')
        else:
            print("\tInput a new address for the contact:")
            new_address = input(">>")
            target_contact.phone_address = new_address
            print("\tSuccessfully updated the contact.")
        return ContactStrategyResult.CONTINUE


class ContactExitStrategy(ContactStrategy):
    @property
    @override
    def description(self: Self) -> str:
        return "Exit"

    @override
    def handle_contacts(
            self: Self,
            contacts: ContactCollection) -> ContactStrategyResult:
        return ContactStrategyResult.EXIT


def main():
    app = ContactManagerApplication(
        ContactPrintStrategy(),
        ContactAddStrategy(),
        ContactRemoveStrategy(),
        ContactUpdateStrategy(),
        ContactExitStrategy())
    app.run()


if __name__ == "__main__":
    main()
