from tkinter import Button, Label, Tk
from tkinter.font import Font
from typing import Callable, Optional, Self


class Model:
    __value: int
    __binder: Optional[Callable[[int], None]]

    def __init__(self: Self, value: int = 0) -> None:
        self.__value = value
        self.__binder = None

    def bind(self: Self, binder: Callable[[int], None]):
        self.__binder = binder

    @property
    def value(self: Self) -> int:
        return self.__value

    @value.setter
    def value(self: Self, value: int) -> None:
        # don't accept negative values
        if value < 0:
            return
        self.__value = value
        self.__binder(self.__value)

    def increase(self: Self) -> None:
        self.value += 1

    def decrease(self: Self) -> None:
        self.value -= 1


class ViewModel:
    model: Model

    def __init__(self: Self, model: Model) -> None:
        self.model = model

    def bind(self: Self, binder: Callable[[int], None]) -> None:
        self.model.bind(binder)

    @property
    def value(self: Self) -> None:
        return self.model.value

    def increase(self: Self) -> None:
        self.model.increase()

    def decrease(self: Self) -> None:
        self.model.decrease()


class View:
    view_model: ViewModel
    label: Label
    increase_button: Button
    decrease_button: Button

    def __init__(
            self: Self,
            window: Tk,
            viewModel: ViewModel) -> None:
        FONT = Font(size=16)
        self.view_model = viewModel
        self.label = Label(window, width=15, height=5, font=FONT)
        self.increase_button = Button(
            window,
            text="Increase Value",
            width=15,
            height=1,
            command=self.view_model.increase,
            font=FONT)
        self.decrease_button = Button(
            window,
            text="Decrease Value",
            width=15,
            height=1,
            command=self.view_model.decrease,
            font=FONT)

        self.view_model.bind(self.render_label)
        self.render_label()

    def pack(self: Self) -> None:
        self.label.pack()
        self.increase_button.pack()
        self.decrease_button.pack()

    def render_label(self: Self, value: Optional[int] = None) -> None:
        if value is None:
            self.label.config(text=f"current value: {self.view_model.value}")
            return
        self.label.config(text=f"current value: {value}")


class Application:
    window: Tk
    model: Model
    view: View
    view_model: ViewModel

    def __init__(self: Self) -> None:
        self.window = Tk()
        self.model = Model()
        self.view_model = ViewModel(self.model)
        self.view = View(self.window, self.view_model)
        self.initialize()

    def initialize(self: Self) -> None:
        self.window.title("MVVM Example")
        self.window.geometry("480x360+50+50")
        self.window.resizable(False, False)
        self.view.pack()

    def start(self: Self) -> None:
        self.window.mainloop()


if __name__ == "__main__":
    app = Application()
    app.start()
