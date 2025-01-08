from dataclasses import dataclass
from typing import Self


@dataclass
class Contact:
    """
    Represents a contact.
    """

    name: str
    phone_address: str


class ContactManagerApplication:
    contacts: list[Contact]

    def __init__(self: Self) -> None:
        self.contacts = []

    def run(self: Self):
        command = self.fetch_command()
        while command != 5:
            if command == 1:
                print("Print Contacts:")
                if len(self.contacts) == 0:
                    print("\tNo contact is registered.")
                else:
                    for contact in self.contacts:
                        print(f"\t{contact}")
            elif command == 2:
                print("Add Contact:")
                print("\tInput the name for new contact:")
                name = input(">>")
                print("\tInput the phone address for new contact:")
                phone_address = input(">>")
                contact = Contact(name, phone_address)
                self.contacts.append(contact)
            elif command == 3:
                print("Remove Contact:")
                print("\tInput the name to remove contact:")
                name = input(">>")
                target_contact = None
                for contact in self.contacts:
                    if contact.name == name:
                        target_contact = contact
                        break
                if target_contact is None:
                    print(f'Cannot find any contacts with the name "{name}".')
                else:
                    self.contacts.remove(target_contact)
                    print("\tSuccessfully removed the contact.")
            elif command == 4:
                print("Update Contact:")
                print("\tInput the name to update contact:")
                name = input(">>")
                target_contact = None
                for contact in self.contacts:
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
            command = self.fetch_command()

    def print_task_info(self: Self) -> None:
        print("------------------------------------------------")
        print("Input the number for the task which you want.")
        print("1. Print contacts")
        print("2. Add contact")
        print("3. Remove contact")
        print("4. Update contact")
        print("5. Exit")

    def fetch_command(self: Self) -> int:
        self.print_task_info()
        command = int(input(">>"))
        while command < 1 or command > 5:
            print("Invalid input found.")
            self.print_task_info()
            command = int(input(">>"))
        return command


def main():
    app = ContactManagerApplication()
    app.run()


if __name__ == "__main__":
    main()
