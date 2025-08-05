package com.toonystank.requisiteteams.gui.system.builder.item;

import com.toonystank.requisiteteams.gui.system.components.exception.GuiException;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Item builder for {@link Material#WRITTEN_BOOK} and {@link Material#WRITTEN_BOOK} only
 *
 */
public class BookBuilder extends BaseItemBuilder<BookBuilder> {

    private static final EnumSet<Material> BOOKS = EnumSet.of(Material.WRITABLE_BOOK, Material.WRITTEN_BOOK);

    BookBuilder(@NotNull ItemStack itemStack) {
        super(itemStack);
        if (!BOOKS.contains(itemStack.getType())) {
            throw new GuiException("BookBuilder requires the material to be a WRITABLE_BOOK/WRITTEN_BOOK!");
        }
    }

    /**
     * Sets the author of the book. Removes author when given null.
     *
     * @param author the author to set
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public BookBuilder author(@Nullable final Component author) {
        final BookMeta bookMeta = (BookMeta) getMeta();

        if (author == null) {
            bookMeta.author(null);
            setMeta(bookMeta);
            return this;
        }
        bookMeta.author(author);
        setMeta(bookMeta);
        return this;
    }

    /**
     * Sets the generation of the book. Removes generation when given null.
     *
     * @param generation the generation to set
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public BookBuilder generation(@Nullable final BookMeta.Generation generation) {
        final BookMeta bookMeta = (BookMeta) getMeta();

        bookMeta.setGeneration(generation);
        setMeta(bookMeta);
        return this;
    }

    /**
     * Adds new pages to the end of the book. Up to a maximum of 50 pages with
     * 256 characters per page.
     *
     * @param pages list of pages
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public BookBuilder page(@NotNull final Component... pages) {
        return page(Arrays.asList(pages));
    }

    /**
     * Adds new pages to the end of the book. Up to a maximum of 50 pages with
     * 256 characters per page.
     *
     * @param pages list of pages
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public BookBuilder page(@NotNull final List<Component> pages) {
        final BookMeta bookMeta = (BookMeta) getMeta();

        for (final Component page : pages) {
            bookMeta.addPages(page);
        }

        setMeta(bookMeta);
        return this;
    }

    /**
     * Sets the specified page in the book. Pages of the book must be
     * contiguous.
     * <p>
     * The data can be up to 256 characters in length, additional characters
     * are truncated.
     * <p>
     * Pages are 1-indexed.
     *
     * @param page the page number to set, in range [1, {@link BookMeta#getPageCount()}]
     * @param data the data to set for that page
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_, _ -> this")
    public BookBuilder page(final int page, @NotNull final Component data) {
        final BookMeta bookMeta = (BookMeta) getMeta();

        bookMeta.page(page, data);
        setMeta(bookMeta);
        return this;
    }

    /**
     * Sets the title of the book.
     * <p>
     * Limited to 32 characters. Removes title when given null.
     *
     * @param title the title to set
     * @return {@link BookBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public BookBuilder title(@Nullable Component title) {
        final BookMeta bookMeta = (BookMeta) getMeta();

        if (title == null) {
            bookMeta.title(null);
            setMeta(bookMeta);
            return this;
        }

        bookMeta.title(title);
        setMeta(bookMeta);
        return this;
    }

}
