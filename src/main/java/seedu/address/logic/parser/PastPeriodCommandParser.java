package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIMESTAMP;

import java.util.stream.Stream;

import seedu.address.logic.commands.budget.PastPeriodCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.expense.Timestamp;

/**
 * Parses input arguments and creates a new PastPeriodCommand object
 */
public class PastPeriodCommandParser {
    /**
     * Parses the given {@code String} of arguments in the context of the PastPeriodCommand
     * and returns a PastPeriodCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public PastPeriodCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_TIMESTAMP);

        if (!arePrefixesPresent(argMultimap, PREFIX_TIMESTAMP)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PastPeriodCommand.MESSAGE_USAGE));
        }

        Timestamp pastDate = ParserUtil.parseTimestamp(argMultimap.getValue(PREFIX_TIMESTAMP).get()).toStartOfDay();

        return new PastPeriodCommand(pastDate);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}