package seedu.moolah.model;

import static java.util.Objects.requireNonNull;
import static seedu.moolah.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.moolah.logic.Timekeeper.hasTranspired;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.moolah.commons.core.GuiSettings;
import seedu.moolah.commons.core.LogsCenter;
import seedu.moolah.model.alias.Alias;
import seedu.moolah.model.alias.AliasMappings;
import seedu.moolah.model.budget.Budget;
import seedu.moolah.model.event.Event;
import seedu.moolah.model.expense.Expense;
import seedu.moolah.model.general.Description;
import seedu.moolah.model.general.Timestamp;
import seedu.moolah.model.modelhistory.ModelChanges;
import seedu.moolah.model.modelhistory.ModelHistory;
import seedu.moolah.model.modelhistory.ReadOnlyModelHistory;
import seedu.moolah.model.statistics.Statistics;

/**
 * Represents the in-memory model of the MooLah data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final MooLah mooLah;
    private final UserPrefs userPrefs;
    private final ModelHistory modelHistory;
    private final FilteredList<Expense> filteredExpenses;
    private final FilteredList<Event> filteredEvents;
    private final FilteredList<Budget> filteredBudgets;
    private Statistics statistics;

    /**
     * Initializes a ModelManager with the given MooLah, UserPrefs, and ModelHistory.
     */
    public ModelManager(ReadOnlyMooLah mooLah, ReadOnlyUserPrefs userPrefs, ReadOnlyModelHistory modelHistory) {
        requireAllNonNull(mooLah, userPrefs, modelHistory);

        logger.fine("Initializing with MooLah: " + mooLah + " and user prefs " + userPrefs);

        this.mooLah = new MooLah(mooLah);
        this.userPrefs = new UserPrefs(userPrefs);
        this.modelHistory = new ModelHistory(modelHistory);
        filteredEvents = new FilteredList<>(this.mooLah.getEventList());
        filteredExpenses = new FilteredList<>(this.mooLah.getExpenseList());
        filteredBudgets = new FilteredList<>(this.mooLah.getBudgetList());
    }

    public ModelManager() {
        this(new MooLah(), new UserPrefs(), new ModelHistory());
    }

    /**
     * Copy constructor for ModelManager.
     * @param model the {@code ModelManager} object to be copied
     */
    public ModelManager(Model model) {
        this();
        resetData(model);
    }

    @Override
    public void resetData(Model model) {
        requireNonNull(model);
        setMooLah(model.getMooLah());
        setUserPrefs(model.getUserPrefs());
        setModelHistory(model.getModelHistory());
        updateFilteredExpenseList(model.getFilteredExpensePredicate());
        updateFilteredEventList(model.getFilteredEventPredicate());
        updateFilteredBudgetList(model.getFilteredBudgetPredicate());
    }

    @Override
    public Model copy() {
        return new ModelManager(this);
    }

    @Override
    public void applyChanges(ModelChanges changes) {
        requireNonNull(changes);
        changes.getMooLah().ifPresent(this::setMooLah);
        changes.getUserPrefs().ifPresent(this::setUserPrefs);
        changes.getExpensePredicate().ifPresent(this::updateFilteredExpenseList);
        changes.getEventPredicate().ifPresent(this::updateFilteredEventList);
        changes.getBudgetPredicate().ifPresent(this::updateFilteredBudgetList);
    }

    //=========== ModelHistory ==================================================================================

    @Override
    public ReadOnlyModelHistory getModelHistory() {
        return modelHistory;
    }

    @Override
    public void setModelHistory(ReadOnlyModelHistory modelHistory) {
        requireNonNull(modelHistory);
        this.modelHistory.resetData(modelHistory);
    }

    @Override
    public void addToPastChanges(ModelChanges change) {
        requireNonNull(change);
        modelHistory.addToPastChanges(change);
    }

    @Override
    public void addToFutureChanges(ModelChanges change) {
        requireNonNull(change);
        modelHistory.addToFutureChanges(change);
    }

    @Override
    public void commit(String changeMessage, Model prevModel) {
        requireAllNonNull(changeMessage, prevModel);
        ModelChanges changes = ModelChanges.compareModels(changeMessage, prevModel, this);
        modelHistory.addToPastChanges(changes);
        modelHistory.clearFutureChanges();
    }

    @Override
    public boolean canRollback() {
        return !modelHistory.isPastChangesEmpty();
    }

    @Override
    public Optional<String> rollback() {
        Optional<ModelChanges> prevChanges = modelHistory.getPrevChanges();
        if (prevChanges.isPresent()) {
            ModelChanges changes = prevChanges.get();
            modelHistory.addToFutureChanges(changes.revertChanges(this));
            applyChanges(changes);
            handleAlreadyTranspiredEvents();
            return Optional.of(changes.getChangeMessage());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean canMigrate() {
        return !modelHistory.isFutureChangesEmpty();
    }

    @Override
    public Optional<String> migrate() {
        Optional<ModelChanges> nextChanges = modelHistory.getNextChanges();
        if (nextChanges.isPresent()) {
            ModelChanges changes = nextChanges.get();
            modelHistory.addToPastChanges(changes.revertChanges(this));
            applyChanges(changes);
            return Optional.of(changes.getChangeMessage());
        } else {
            return Optional.empty();
        }
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs.copy());
    }

    @Override
    public Path getMooLahFilePath() {
        return userPrefs.getMooLahFilePath();
    }

    @Override
    public void setMooLahFilePath(Path mooLahFilePath) {
        requireNonNull(mooLahFilePath);
        userPrefs.setMooLahFilePath(mooLahFilePath);
    }

    //=========== AliasSettings ==============================================================================

    @Override
    public AliasMappings getAliasMappings() {
        return userPrefs.getAliasMappings();
    }

    @Override
    public void setAliasMappings(AliasMappings aliasMappings) {
        requireNonNull(aliasMappings);
        userPrefs.setAliasMappings(aliasMappings);
    }

    @Override
    public void addUserAlias(Alias alias) {
        userPrefs.addUserAlias(alias);
    }

    public boolean aliasWithNameExists(String aliasName) {
        return userPrefs.hasAlias(aliasName);
    }

    @Override
    public boolean removeAliasWithName(String aliasName) {
        return userPrefs.removeAliasWithName(aliasName);
    }

    //=========== GuiSettings ===============================================================================

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== Expense ================================================================================

    @Override
    public ReadOnlyMooLah getMooLah() {
        return mooLah;
    }

    @Override
    public void setMooLah(ReadOnlyMooLah mooLah) {
        requireNonNull(mooLah);
        this.mooLah.resetData(mooLah);
    }

    @Override
    public boolean hasExpense(Expense expense) {
        requireNonNull(expense);
        return mooLah.hasExpense(expense);
    }

    @Override
    public void deleteExpense(Expense target) {
        mooLah.removeExpense(target);
    }

    @Override
    public void addExpense(Expense expense) {
        mooLah.addExpense(expense);
        updateFilteredExpenseList(PREDICATE_SHOW_ALL_EXPENSES);
    }

    @Override
    public void setExpense(Expense target, Expense editedExpense) {
        requireAllNonNull(target, editedExpense);

        mooLah.setExpense(target, editedExpense);
    }

    //=========== Budget ================================================================================

    /**
     * Checks whether the model contains an equivalent budget as the given argument.
     *
     * @param budget The budget to check.
     * @return True if the model contains budget identical to the budget to check.
     */
    @Override
    public boolean hasBudget(Budget budget) {
        requireNonNull(budget);
        return mooLah.hasBudget(budget);
    }

    /**
     * Adds a budget to the model.
     *
     * @param budget The budget to be added.
     */
    @Override
    public void addBudget(Budget budget) {
        mooLah.addBudget(budget);
    }

    /**
     * Returns true if there is a budget with the specified description in the model.
     *
     * @param targetDescription The specified description.
     * @return True if there exists a budget with the specified description in the model, false otherwise.
     */
    @Override
    public boolean hasBudgetWithName(Description targetDescription) {
        return mooLah.hasBudgetWithName(targetDescription);
    }

    /**
     * Returns the primary budget in this model.
     *
     * @return The primary budget in this model.
     */
    @Override
    public Budget getPrimaryBudget() {
        return mooLah.getPrimaryBudget();
    }

    /**
     * Checks if the model has a primary budget.
     *
     * @return True if the model has a primary budget, false otherwise.
     */
    @Override
    public boolean hasPrimaryBudget() {
        return getPrimaryBudget() != null;
    }

    /**
     * Switches the primary budget to the budget with the specified description.
     *
     * @param targetDescription The specified description.
     */
    @Override
    public void switchBudgetTo(Description targetDescription) {
        mooLah.switchBudgetTo(targetDescription);
    }

    /**
     * Removes a budget from this model.
     *
     * @param target The budget to remove.
     */
    @Override
    public void deleteBudget(Budget target) {
        mooLah.removeBudget(target);
    }

    /**
     * Replaces the target budget with an edited budget.
     *
     * @param target The budget to be replaced.
     * @param editedBudget The updated budget.
     */
    @Override
    public void setBudget(Budget target, Budget editedBudget) {
        requireAllNonNull(target, editedBudget);

        mooLah.setBudget(target, editedBudget);
    }

    /**
     * Changes window of primary budget to a different window anchored by the specified date.
     *
     * @param pastDate A date in the past to anchor the window to be switched to.
     */
    @Override
    public void changePrimaryBudgetWindow(Timestamp pastDate) {
        requireAllNonNull(pastDate);

        mooLah.changePrimaryBudgetWindow(pastDate);
    }

    /** Clears all budgets in the model, except the default budget. */
    @Override
    public void clearBudgets() {
        mooLah.clearBudgets();
    }

    /**
     * Deletes the budget with the specified description from the model.
     *
     * @param description The specified description.
     */
    @Override
    public void deleteBudgetWithName(Description description) {
        mooLah.deleteBudgetWithName(description);
    }

    //=========== Event ================================================================================

    @Override
    public boolean hasEvent(Event event) {
        requireNonNull(event);
        return mooLah.hasEvent(event);
    }

    @Override
    public void deleteEvent(Event target) {
        mooLah.removeEvent(target);
    }

    @Override
    public void addEvent(Event event) {
        mooLah.addEvent(event);
        updateFilteredEventList(PREDICATE_SHOW_ALL_EVENTS);
    }

    @Override
    public void setEvent(Event target, Event editedEvent) {
        requireAllNonNull(target, editedEvent);

        mooLah.setEvent(target, editedEvent);
    }

    /**
     * Deletes all transpired events right after a model rollback so as to prevent repeated event popups arising
     * from the undo feature.
     */
    @Override
    public void handleAlreadyTranspiredEvents() {
        List<Event> toBeDeleted = new ArrayList<>();
        for (Event event : filteredEvents) {
            if (hasTranspired(event.getTimestamp())) {
                toBeDeleted.add(event);
            }
        }

        for (Event event : toBeDeleted) {
            deleteEvent(event);
        }
    }

    //=========== Statistics ================================================================================

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        requireNonNull(statistics);
        this.statistics = statistics;
    }


    //=========== Filtered Expense List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Expense} backed by the internal list of
     * {@code versionedMooLah}
     */
    @Override
    public ObservableList<Expense> getFilteredExpenseList() {
        return filteredExpenses;
    }

    @Override
    public Predicate<? super Expense> getFilteredExpensePredicate() {
        if (filteredExpenses.getPredicate() == null) {
            return PREDICATE_SHOW_ALL_EXPENSES;
        }

        return filteredExpenses.getPredicate();
    }

    @Override
    public void updateFilteredExpenseList(Predicate<? super Expense> predicate) {
        requireNonNull(predicate);
        filteredExpenses.setPredicate(predicate);
    }

    //=========== Filtered Event List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Event} backed by the internal list of
     * {@code versionedMooLah}
     */
    @Override
    public ObservableList<Event> getFilteredEventList() {
        return filteredEvents;
    }

    @Override
    public Predicate<? super Event> getFilteredEventPredicate() {
        if (filteredEvents.getPredicate() == null) {
            return PREDICATE_SHOW_ALL_EVENTS;
        }

        return filteredEvents.getPredicate();
    }

    @Override
    public void updateFilteredEventList(Predicate<? super Event> predicate) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(predicate);
    }

    //=========== Filtered Budget List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Budget} backed by the internal list of
     * {@code versionedMooLah}
     */
    @Override
    public ObservableList<Budget> getFilteredBudgetList() {
        return filteredBudgets;
    }

    @Override
    public void updateFilteredBudgetList(Predicate<? super Budget> predicate) {
        requireNonNull(predicate);
        filteredBudgets.setPredicate(predicate);
    }

    @Override
    public Predicate<? super Budget> getFilteredBudgetPredicate() {
        if (filteredBudgets.getPredicate() == null) {
            return PREDICATE_SHOW_ALL_BUDGETS;
        }

        return filteredBudgets.getPredicate();
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
        return mooLah.equals(other.mooLah)
                && userPrefs.equals(other.userPrefs)
                && filteredExpenses.equals(other.filteredExpenses)
                && filteredEvents.equals(other.filteredEvents)
                && modelHistory.equals(other.modelHistory);
    }
}
