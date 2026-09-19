package com.playwright.framework.components;

import com.microsoft.playwright.Page;
import com.playwright.framework.ui.UiElement;

/**
 * Generic table component: rows are exposed as {@link UiElement}s so callers can
 * chain fluent actions/assertions. Pass in any Locator that represents a
 * {@code <table>} or ARIA {@code role="table"} root.
 */
public class TableComponent extends BaseComponent {

    public TableComponent(Page page, com.microsoft.playwright.Locator root, String name) {
        super(page, root, name);
    }

    public UiElement header() {
        return $("thead", "header row");
    }

    public UiElement rows() {
        return $("tbody tr", "body rows");
    }

    public UiElement row(int index) {
        return rows().nth(index);
    }

    public int rowCount() {
        return rows().count();
    }

    public UiElement cell(int rowIndex, int columnIndex) {
        return rows().nth(rowIndex).child("td:nth-child(" + (columnIndex + 1) + ")", "cell[" + rowIndex + "," + columnIndex + "]");
    }
}
