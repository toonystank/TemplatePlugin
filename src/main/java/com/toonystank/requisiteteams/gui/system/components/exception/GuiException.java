package com.toonystank.requisiteteams.gui.system.components.exception;

public final class GuiException extends RuntimeException {

    public GuiException(String message) {
        super(message);
    }

    public GuiException(String message, Exception cause) {
        super(message, cause);
    }
}
