from abc import ABC, abstractmethod
from tkinter import Button, Label, Tk
from tkinter.font import Font
from typing import Callable, Optional, Self, override


class Model:
    value: int

    def __init__(self: Self, value: int = 0) -> None:
        self.value = value


class Controller:
    model: Model

    def __init__(self: Self, model: Model) -> None:
        self.model = model

    def increase(self: Self) -> None:
        self.model.value += 1

    def decrease(self: Self) -> None:
        self.model.value -= 1


class View:

    model: Model
    controller: Controller
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
        self.controller = controller
        self.label = Label(window, width=15, height=5, font=FONT)
        self.increase_button = Button(
            window,
            text="Increase Value",
            width=15,
            height=1,
            command=self.on_decrease_button_click,
            font=FONT)
        self.decrease_button = Button(
            window,
            text="Decrease Value",
            width=15,
            height=1,
            command=self.on_decrease_button_click,
            font=FONT)

        self.render_label()

    def pack(self: Self) -> None:
        self.label.pack()
        self.increase_button.pack()
        self.decrease_button.pack()

    def on_increase_button_click(self: Self) -> None:
        self.controller.increase()
        self.render_label()

    def on_decrease_button_click(self: Self) -> None:
        self.controller.decrease()
        self.render_label()

    def render_label(self: Self) -> None:
        self.label.config(text=f"current value: {self.model.value}")


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
