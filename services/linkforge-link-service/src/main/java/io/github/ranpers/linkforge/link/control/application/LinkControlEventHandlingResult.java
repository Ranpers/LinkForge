package io.github.ranpers.linkforge.link.control.application;

public record LinkControlEventHandlingResult(Status status, int changedRows) {

    public enum Status {
        APPLIED,
        DUPLICATE,
        STALE
    }

    public static LinkControlEventHandlingResult applied(int changedRows) {
        return new LinkControlEventHandlingResult(Status.APPLIED, changedRows);
    }

    public static LinkControlEventHandlingResult duplicate() {
        return new LinkControlEventHandlingResult(Status.DUPLICATE, 0);
    }

    public static LinkControlEventHandlingResult stale() {
        return new LinkControlEventHandlingResult(Status.STALE, 0);
    }
}
