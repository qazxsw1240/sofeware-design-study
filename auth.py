from __future__ import annotations  # import for type hint in static methods

import json
from abc import ABC, abstractmethod
from dataclasses import dataclass
from getpass import getpass
from typing import Optional, Self, override


@dataclass
class Config:
    username: str
    password: str


class ConfigLoaderBase(ABC):
    @abstractmethod
    def load(self: Self) -> Config:
        pass


class AuthInputServiceBase(ABC):
    @abstractmethod
    def fetch_config(self: Self) -> Config:
        pass


class JsonConfigLoader(ConfigLoaderBase):
    filename: str

    def __init__(self: Self, filename: str) -> None:
        self.filename = filename

    @override
    def load(self: Self) -> Config:
        with open(self.filename, "r") as file:
            text = file.read()
            data = json.loads(text)
            name = data["username"]
            password = data["password"]
            return Config(name, password)


class AuthInputService(AuthInputServiceBase):
    def fetch_config(self: Self) -> Config:
        name = input("Username: ")
        password = getpass("Password: ")
        return Config(name, password)


class AuthApp:
    config_loader: ConfigLoaderBase
    input_service: AuthInputServiceBase
    config: Config
    auth: bool

    def __init__(
            self: Self,
            config_loader: ConfigLoaderBase,
            input_service: AuthInputServiceBase) -> None:
        self.config_loader = config_loader
        self.input_service = input_service
        self.config = self.config_loader.load()
        self.auth = False

    @property
    def is_auth(self: Self) -> bool:
        return self.auth

    def authenticate(self: Self) -> None:
        if self.auth:
            return
        config = self.input_service.fetch_config()
        if config != self.config:
            raise Exception("Failed authentication.")
        self.auth = True


class AuthAppFactory(ABC):
    @staticmethod
    def create(factory: Optional[AuthAppFactory] = None) -> AuthApp:
        if factory is None:
            factory = DefaultAuthAppFactory()
        config_loader = factory.create_config_loader()
        input_service = factory.create_input_service()
        return AuthApp(config_loader, input_service)

    @abstractmethod
    def create_config_loader(self: Self) -> ConfigLoaderBase:
        pass

    @abstractmethod
    def create_input_service(self: Self) -> AuthInputService:
        pass


class DefaultAuthAppFactory(AuthAppFactory):
    @override
    def create_config_loader(self: Self) -> ConfigLoaderBase:
        return JsonConfigLoader("./config.json")

    @override
    def create_input_service(self: Self) -> AuthInputService:
        return AuthInputService()


def main():
    app = AuthAppFactory.create()
    app.authenticate()
    if app.auth:
        print("Successfully authenticated.")


if __name__ == "__main__":
    main()
