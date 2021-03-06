package seedu.moolah.logic.commands.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.moolah.logic.commands.CommandTestUtil.VALID_STATS_DESCRIPTOR;
import static seedu.moolah.logic.commands.CommandTestUtil.assertCommandSuccess;

import static seedu.moolah.testutil.TypicalMooLah.getTypicalMooLahForStatistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.moolah.logic.commands.general.ClearCommand;
import seedu.moolah.model.Model;
import seedu.moolah.model.ModelManager;
import seedu.moolah.model.UserPrefs;
import seedu.moolah.model.budget.Budget;
import seedu.moolah.model.modelhistory.ModelHistory;
import seedu.moolah.model.statistics.PieChartStatistics;


/**
 * Contains integration tests (interaction with the Model) and unit tests for StatsCommand.
 */
class StatsCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setup() {
        model = new ModelManager(getTypicalMooLahForStatistics(), new UserPrefs(), new ModelHistory());
        expectedModel = new ModelManager(getTypicalMooLahForStatistics(), new UserPrefs(), new ModelHistory());
    }

    @Test
    public void run_allFieldsSpecifiedStatistics_success() {
        StatsDescriptor descriptor = new StatsDescriptor();

        Budget primaryBudget = model.getPrimaryBudget();
        descriptor.setStartDate(primaryBudget.getWindowStartDate());
        descriptor.setEndDate(primaryBudget.getWindowEndDate());
        StatsCommand command = new StatsCommand(descriptor);

        PieChartStatistics statistics = new PieChartStatistics(primaryBudget.getExpenses(),
                primaryBudget.getWindowStartDate(),
                primaryBudget.getWindowEndDate());
        statistics.populateData();
        expectedModel.setStatistics(statistics);
        String expectedMessage = StatsCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }


    @Test
    public void run_startDateSpecifiedStatistics_success() {
        StatsDescriptor descriptor = new StatsDescriptor();

        Budget primaryBudget = model.getPrimaryBudget();
        descriptor.setStartDate(primaryBudget.getWindowStartDate());
        StatsCommand command = new StatsCommand(descriptor);

        PieChartStatistics statistics = new PieChartStatistics(primaryBudget.getExpenses(),
                primaryBudget.getWindowStartDate(),
                primaryBudget.getWindowEndDate());
        statistics.populateData();
        expectedModel.setStatistics(statistics);
        String expectedMessage = StatsCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void run_endDateSpecifiedStatistics_success() {
        StatsDescriptor descriptor = new StatsDescriptor();

        Budget primaryBudget = model.getPrimaryBudget();
        descriptor.setEndDate(primaryBudget.getWindowEndDate());
        StatsCommand command = new StatsCommand(descriptor);

        PieChartStatistics statistics = new PieChartStatistics(primaryBudget.getExpenses(),
                primaryBudget.getWindowStartDate(),
                primaryBudget.getWindowEndDate());
        statistics.populateData();
        expectedModel.setStatistics(statistics);
        String expectedMessage = StatsCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }


    @Test
    public void run_noFieldSpecifiedStatistics_success() {
        StatsDescriptor descriptor = new StatsDescriptor();

        Budget primaryBudget = model.getPrimaryBudget();
        StatsCommand command = new StatsCommand(descriptor);

        PieChartStatistics statistics = new PieChartStatistics(primaryBudget.getExpenses(),
                primaryBudget.getWindowStartDate(),
                primaryBudget.getWindowEndDate());
        statistics.populateData();
        expectedModel.setStatistics(statistics);
        String expectedMessage = StatsCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        final StatsCommand standardCommand = new StatsCommand(VALID_STATS_DESCRIPTOR);

        // same values -> returns true
        StatsDescriptor copyDescriptor = new StatsDescriptor(VALID_STATS_DESCRIPTOR);
        StatsCommand commandWithSameValues = new StatsCommand(copyDescriptor);
        assertEquals(standardCommand, commandWithSameValues);

        // same object -> returns true
        assertEquals(standardCommand, standardCommand);

        // null -> returns false
        assertNotEquals(null, standardCommand);

        // different types -> returns false
        assertNotEquals(standardCommand, new ClearCommand());
    }



}
