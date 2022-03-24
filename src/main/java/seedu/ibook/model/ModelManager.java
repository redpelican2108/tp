package seedu.ibook.model;

import static java.util.Objects.requireNonNull;
import static seedu.ibook.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.ibook.commons.core.GuiSettings;
import seedu.ibook.commons.core.LogsCenter;
import seedu.ibook.model.item.Item;
import seedu.ibook.model.product.Product;
import seedu.ibook.model.product.filters.AttributeFilter;
import seedu.ibook.model.product.filters.ProductFilter;

/**
 * Represents the in-memory model of the iBook data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final IBook iBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Product> filteredProducts;
    private final ProductFilter productFilter;

    /**
     * Initializes a ModelManager with the given iBook and userPrefs.
     */
    public ModelManager(ReadOnlyIBook iBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(iBook, userPrefs);

        logger.fine("Initializing with iBook: " + iBook + " and user prefs " + userPrefs);

        this.iBook = new IBook(iBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredProducts = new FilteredList<>(this.iBook.getProductList());
        productFilter = new ProductFilter();

        filteredProducts.setPredicate(productFilter);
    }

    public ModelManager() {
        this(new IBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getIBookFilePath() {
        return userPrefs.getIBookFilePath();
    }

    @Override
    public void setIBookFilePath(Path iBookFilePath) {
        requireNonNull(iBookFilePath);
        userPrefs.setIBookFilePath(iBookFilePath);
    }

    //=========== iBook ================================================================================

    @Override
    public void setIBook(ReadOnlyIBook iBook) {
        this.iBook.resetData(iBook);
    }

    @Override
    public ReadOnlyIBook getIBook() {
        return iBook;
    }

    //=========== Product =====================================================================================
    @Override
    public boolean hasProduct(Product product) {
        requireNonNull(product);
        return iBook.hasProduct(product);
    }

    @Override
    public void deleteProduct(Product target) {
        iBook.removeProduct(target);
    }

    @Override
    public void addProduct(Product product) {
        iBook.addProduct(product);
        clearProductFilters();
    }

    @Override
    public void setProduct(Product target, Product editedProduct) {
        requireAllNonNull(target, editedProduct);
        iBook.setProduct(target, editedProduct);
    }

    //=========== Item ========================================================================================

    @Override
    public void addItem(Product product, Item item) {
        requireAllNonNull(product, item);
        iBook.addItem(product, item);
    }

    @Override
    public void deleteItem(Product targetProduct, Item target) {
        requireAllNonNull(targetProduct, target);
        iBook.removeItem(targetProduct, target);
    }

    @Override
    public void updateItem(Product targetProduct, Item targetItem, Item updatedItem) {
        requireAllNonNull(targetProduct, targetItem, updatedItem);
        iBook.setItem(targetProduct, targetItem, updatedItem);
    }

    //=========== Filtered Product List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Product} backed by the internal list of
     * {@code versionedIBook}
     */
    @Override
    public ObservableList<Product> getFilteredProductList() {
        return filteredProducts;
    }

    /**
     * TODO: Hack to refresh the filtered list. Feel free to improve this.
     */
    private void refreshFilteredProductList() {
        filteredProducts.setPredicate(unused -> true);
        filteredProducts.setPredicate(productFilter);
    }

    /**
     * Adds a filter to the product list.
     */
    @Override
    public void addProductFilter(AttributeFilter filter) {
        productFilter.addFilter(filter);
        refreshFilteredProductList();
    }

    /**
     * Removes a filter from the product list.
     */
    @Override
    public void removeProductFilter(AttributeFilter filter) {
        productFilter.removeFilter(filter);
        refreshFilteredProductList();
    }

    /**
     * Removes a filter from the product list.
     */
    @Override
    public void clearProductFilters() {
        productFilter.clearFilters();
        refreshFilteredProductList();
    }

    // TODO: remove this in the future so that product filter would not be changed
    @Override
    public void updateProductFilters(Predicate<Product> predicate) {
        requireNonNull(predicate);
        filteredProducts.setPredicate(predicate);
    }

    @Override
    public ObservableList<AttributeFilter> getProductFilters() {
        return productFilter.getFilters();
    }

    @Override
    public void updateFilteredItemListForProducts(Predicate<Item> predicate) {
        for (Product p: filteredProducts) {
            p.updateFilteredItemList(predicate);
        }
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return iBook.equals(other.iBook)
                && userPrefs.equals(other.userPrefs)
                && filteredProducts.equals(other.filteredProducts);
    }
}