package seedu.moolah.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.moolah.commons.core.Messages.MESSAGE_INVALID_EXPENSE_DISPLAYED_INDEX;
import static seedu.moolah.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.moolah.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.moolah.logic.commands.CommandResult;
import seedu.moolah.logic.commands.exceptions.CommandException;
import seedu.moolah.logic.commands.expense.ListExpensesCommand;
import seedu.moolah.logic.parser.exceptions.ParseException;
import seedu.moolah.model.Model;
import seedu.moolah.model.ModelManager;
import seedu.moolah.model.ReadOnlyMooLah;
import seedu.moolah.model.UserPrefs;
import seedu.moolah.storage.JsonMooLahStorage;
import seedu.moolah.storage.JsonUserPrefsStorage;
import seedu.moolah.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonMooLahStorage mooLahStorage =
                new JsonMooLahStorage(temporaryFolder.resolve("moolah.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(mooLahStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
        Timekeeper timekeeper = new Timekeeper(logic);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "deleteexpense 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_EXPENSE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListExpensesCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListExpensesCommand.MESSAGE_SUCCESS, model);
    }

    //    @Test
    //    public void execute_storageThrowsIoException_throwsCommandException() {
    //        // Setup LogicManager with JsonMooLahIoExceptionThrowingStub
    //        JsonMooLahStorage addressBookStorage =
    //                new JsonMooLahIoExceptionThrowingStub(
    //                        temporaryFolder.resolve("ioExceptionAddressBook.json"));
    //        JsonUserPrefsStorage userPrefsStorage =
    //                new JsonUserPrefsStorage(temporaryFolder.resolve("ioExceptionUserPrefs.json"));
    //        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
    //        Timekeeper timekeeper = new Timekeeper(model);
    //        logic = new LogicManager(model, storage, timekeeper);

    // Execute add command
    //        String addCommand = AddExpenseCommand.COMMAND_WORD + DESCRIPTION_DESC_CHICKEN + PRICE_DESC_CHICKEN;
    //        Expense expectedExpense = new ExpenseBuilder(CHICKEN).withTags().build();
    //        ModelManager expectedModel = new ModelManager();
    //        expectedModel.addExpense(expectedExpense);
    //        String expectedMessage = LogicManager.FILE_OPS_ERROR_MESSAGE + DUMMY_IO_EXCEPTION;
    //        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    //    }

    @Test
    public void getFilteredExpenseList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredExpenseList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand, "");
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getMooLah(), new UserPrefs(), model.getModelHistory());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand, ""));
        assertEquals(expectedModel, model);
    }

    /**
     * A stub class to throw an {@code IOException} when the save method is called.
     */
    private static class JsonMooLahIoExceptionThrowingStub extends JsonMooLahStorage {
        private JsonMooLahIoExceptionThrowingStub(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveMooLah(ReadOnlyMooLah mooLah, Path filePath) throws IOException {
            throw DUMMY_IO_EXCEPTION;
        }
    }
}
