from abc import ABC, abstractmethod
from tkinter import Button, Label, Tk
from tkinter.font import Font
from typing import Callable, Optional, Self, override


class ValueHolder[T](ABC):
    @abstractmethod
    def set_binder(self: Self, binder: Callable[[T], None]) -> None:
        pass


class Model(ValueHolder[int]):
    value: int
    binder: Optional[Callable[[int], None]]

    def __init__(self: Self, value: int = 0) -> None:
        self.value = value
        self.binder = None

    @override
    def set_binder(self: Self, binder: Callable[[int], None]) -> None:
        self.binder = binder
        if self.binder is not None:
            self.binder(self.value)

    def increase(self: Self) -> None:
        self.value += 1
        if self.binder is not None:
            self.binder(self.value)

    def decrease(self: Self) -> None:
        self.value -= 1
        if self.binder is not None:
            self.binder(self.value)


class Controller:
    model: Model

    def __init__(self: Self, model: Model) -> None:
        self.model = model

    def increase(self: Self) -> None:
        self.model.increase()

    def decrease(self: Self) -> None:
        self.model.decrease()


class View:

    model: Model
    label: Label
    increase_button: Button
    decrease_button: Button

    def __init__(
            self: Self,
            window: Tk,
            model: Model,
            controller: Controller) -> None:
        FONT = Font(size=16)
        self.model = model
        self.label = Label(window, width=15, height=5, font=FONT)
        self.increase_button = Button(
            window,
            text="Increase Value",
            width=15,
            height=1,
            command=controller.increase,
            font=FONT)
        self.decrease_button = Button(
            window,
            text="Decrease Value",
            width=15,
            height=1,
            command=controller.decrease,
            font=FONT)
        self.model.set_binder(self.display_value)

    def pack(self: Self) -> None:
        self.label.pack()
        self.increase_button.pack()
        self.decrease_button.pack()

    def display_value(self: Self, value: int) -> None:
        self.label.config(text=f"current value: {value}")


class Application:
    window: Tk
    model: Model
    view: View
    controller: Controller

    def __init__(self: Self) -> None:
        self.window = Tk()
        self.model = Model()
        self.controller = Controller(self.model)
        self.view = View(self.window, self.model, self.controller)
        self.initialize()

    def initialize(self: Self) -> None:
        self.window.title("MVC Example")
        self.window.geometry("480x360+50+50")
        self.window.resizable(False, False)
        self.view.pack()

    def start(self: Self) -> None:
        self.window.mainloop()


if __name__ == "__main__":
    app = Application()
    app.start()
