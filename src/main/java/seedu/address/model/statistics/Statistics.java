package seedu.address.model.statistics;

import static java.util.Objects.requireNonNull;

import java.time.Period;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.model.category.Category;
import seedu.address.model.expense.Expense;
import seedu.address.model.expense.Timestamp;



/**
 * Represents Statistics in MooLah.
 */
public class Statistics {

    public static final String MESSAGE_CONSTRAINTS_END_DATE = "Start date must be before end date.";

    private ObservableList<Expense> expenses;
    private List<Category> categories;
    //gonna remove soon, currently for debugging into ResultDisplay
    private StringBuilder statsBuilder = new StringBuilder();

    private List<String> formattedCategories;

    private List<Double> formattedPercentages;

    private String title;

    /**
     * Creates a Statistics object
     * @param expenses A list of expenses in the current budget
     * @param categories A list of tags used among all expenses
     */
    private Statistics(ObservableList<Expense> expenses, List<Category> categories) {
        requireNonNull(categories);
        requireNonNull(expenses);
        this.expenses = expenses;
        this.categories = categories;
    }

    /**
     * Returns the lists of all expenses in the current budget
     */
    public ObservableList<Expense> getExpenses() {
        return expenses;
    }

    /**
     * Returns the formatted categories to be used as labels for the PieChart
     * @return
     */
    public List<String> getFormattedCategories() {
        return formattedCategories;
    }

    public List<Double> getFormattedPercentages() {
        return formattedPercentages;
    }

    public String getTitle() {
        return title;
    }

    /**
     * A factory method for creating a Statistics object
     * @param expenses A list of expenses in the current budget
     * @return Statistics object
     */
    public static Statistics startStatistics(ObservableList<Expense> expenses) {
        requireNonNull(expenses);
        List<Category> categories = collateCategoryNames(expenses);
        return new Statistics(expenses, categories);
    }

    /**
     * Calculates the statistics based on the command word given by the user
     * @param command The command word
     * @param startDate The starting date
     * @param endDate The ending date
     * @return The calculated statistics in the form of a StringBuilder
     */
    public StringBuilder calculateStats(String command, Timestamp startDate, Timestamp endDate, Period period) {
        switch (command) {
        case "stats":
            basicStats(startDate, endDate);
            break;
        case "statscompare":
            compareStats(startDate, endDate, period);
            break;
        default:
            break;
        }
        return statsBuilder;
    }

    /**
     * Compares the difference in basic statistics across 2 time periods
     * @param firstStartDate
     * @param secondStartDate
     * @param period
     */
    private void compareStats(Timestamp firstStartDate, Timestamp secondStartDate, Period period) {
        requireNonNull(firstStartDate);
        requireNonNull(secondStartDate);
        requireNonNull(period);

        Timestamp firstEndDate = new Timestamp(firstStartDate.timestamp.plus(period));
        Timestamp secondEndDate = new Timestamp(secondStartDate.timestamp.plus(period));

        ArrayList<ArrayList<Expense>> firstData = extractRelevantExpenses(firstStartDate, firstEndDate);
        ArrayList<ArrayList<Expense>> secondData = extractRelevantExpenses(secondStartDate, secondEndDate);

        statsBuilder.append("Statistics Summary\n");
        statsBuilder.append(String.format("Comparing %s to %s\n", firstStartDate, firstEndDate));
        statsBuilder.append(String.format("With %s to %s\n", secondStartDate, secondEndDate));

        ArrayList<ArrayList<Object>> firstTable = createEmptyTableWithoutPercentage();
        ArrayList<ArrayList<Object>> secondTable = createEmptyTableWithoutPercentage();

        List<String> headers = List.of("Category: ",
                "Amount Spent ($): ",
                "Number of entries: ");

        convertDataToFigures(firstData, firstTable, false);
        convertDataToFigures(secondData, secondTable, false);
        ArrayList<ArrayList<Object>> result = secondMinusFirst(firstTable, secondTable);
        convertTableToString(result, headers);
    }

    /**
     * Creates a new table containing the differences in the two tables of basic statistics
     * @param firstTable The basic statistics of the first time period
     * @param secondTable The basic statistics of the second time period
     * @return A table containing the differences in the two tables of basic statistics
     */
    private ArrayList<ArrayList<Object>> secondMinusFirst(ArrayList<ArrayList<Object>> firstTable,
                                                          ArrayList<ArrayList<Object>> secondTable) {
        ArrayList<ArrayList<Object>> result = createEmptyTableWithoutPercentage();
        int rowSize = firstTable.size();
        int columnSize = firstTable.get(0).size();
        for (int i = 0; i < rowSize; i++) {
            ArrayList<Object> tableEntry = result.get(i);
            for (int j = 0; j < columnSize; j++) {
                Object firstInput = firstTable.get(i).get(j);
                Object secondInput = secondTable.get(i).get(j);
                tableEntry.set(j, handleInputType(firstInput, secondInput));
            }
        }
        return result;
    }

    /**
     * Handles the interaction between the first input and the second input by their type
     * @param firstInput The input from the first table
     * @param secondInput The input from the second table
     * @return The correct result to be placed into the result table
     */
    private Object handleInputType(Object firstInput, Object secondInput) {
        if (firstInput instanceof Category) {
            return firstInput;
        } else if (firstInput instanceof Double) {
            return (Double) secondInput - (Double) firstInput;
        } else if (firstInput instanceof Integer) {
            return (Integer) secondInput - (Integer) firstInput;
        } else {
            return null;
        }
    }

    /**
     * Calculates the basic statistics between 2 dates and stores the results in the statsBuilder field
     * @param startDate The starting date
     * @param endDate The ending date
     */
    private void basicStats(Timestamp startDate, Timestamp endDate) {
        requireNonNull(startDate);
        requireNonNull(endDate);

        ArrayList<ArrayList<Expense>> data = extractRelevantExpenses(startDate, endDate);

        String title = String.format("Statistics Summary from %s to %s\n", startDate, endDate);

        //keeping old methods for debugging
        ArrayList<ArrayList<Object>> table = createEmptyTableWithPercentage();

        ArrayList<Double> percentages = new ArrayList<>();
        ArrayList<Integer> numberOfEntries = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for (Category category : categories) {
            percentages.add(0.0);
            numberOfEntries.add(0);
            names.add(category.getCategoryName());
        }

        List<String> headers = List.of("Category: ",
                "Amount Spent ($): ",
                "Number of entries: ",
                "Percentage: ");
        convertDataToFigures(data, table, true);
        convertTableToString(table, headers);
        //end of debugging

        generatePercentages(data, percentages, numberOfEntries, names, title);
    }

    /**
     * Fills in the data to be passed to a GUI
     * @param data Expenses grouped together under their Categories
     * @param percentages List of all percentages under each category
     * @param numberOfEntries List of number of entries under each category
     * @param names List of all names to be shown in the legend representing the category
     * @param titleWithPeriod String containing the period of time the statistics is taken
     */
    private void generatePercentages(ArrayList<ArrayList<Expense>> data, ArrayList<Double> percentages,
                                     ArrayList<Integer> numberOfEntries, ArrayList<String> names,
                                     String titleWithPeriod) {
        double totalAmount = 0.0;

        for (int i = 0; i < categories.size(); i++) {
            ArrayList<Expense> categoryStats = data.get(i);

            //update entry number and the total
            for (Expense expense : categoryStats) {
                double oldCategoricalTotal = percentages.get(i);
                double price = Double.parseDouble(expense.getPrice().value);
                percentages.set(i, oldCategoricalTotal + price);
                totalAmount += price;
                int oldNumberOfEntries = numberOfEntries.get(i);
                numberOfEntries.set(i, oldNumberOfEntries + 1);
            }
        }

        for (int i = 0; i < categories.size(); i++) {
            double categoricalTotal = percentages.get(i);
            double roundedResult = Math.round(categoricalTotal * 10000 / totalAmount) / 100.0;
            percentages.set(i, roundedResult);
            //append the percentage of total expenditure for each category to its name
            String oldName = names.get(i);
            names.set(i, String.format("%s(%.2f%%)", oldName, roundedResult));
        }

        this.formattedCategories = names;
        this.formattedPercentages = percentages;
        this.title = String.format("%s\nTotal amount: $%.2f", titleWithPeriod, totalAmount);
    }


    //depreciated method
    /**
     * Creates an empty table
     *
     * @return An list of table entries
     */
    private ArrayList<ArrayList<Object>> createEmptyTableWithPercentage() {
        ArrayList<ArrayList<Object>> table = new ArrayList<>();
        for (int i = 0; i < categories.size() + 1; i++) {
            ArrayList<Object> tableEntry = new ArrayList<>();
            table.add(tableEntry);
            tableEntry.add(null);
            tableEntry.add(0.0);
            tableEntry.add(0);
            tableEntry.add(0.0);

        }
        return table;
    }
    //depreciated method


    /**
     * Creates an empty table
     *
     * @return An list of table entries
     */
    private ArrayList<ArrayList<Object>> createEmptyTableWithoutPercentage() {
        ArrayList<ArrayList<Object>> table = new ArrayList<>();
        for (int i = 0; i < categories.size() + 1; i++) {
            ArrayList<Object> tableEntry = new ArrayList<>();
            table.add(tableEntry);
            tableEntry.add(null);
            tableEntry.add(0.0);
            tableEntry.add(0);
        }
        return table;
    }


    /**
     * Fills in the table with calculations from the expenses
     */
    private void convertDataToFigures(ArrayList<ArrayList<Expense>> data,
                                      ArrayList<ArrayList<Object>> table, boolean hasFourColumns) {

        ArrayList<Object> entryForTotal = table.get(categories.size());
        entryForTotal.set(0, "Total");
        //loop all categories
        for (int i = 0; i < categories.size(); i++) {
            ArrayList<Expense> categoryStats = data.get(i);
            ArrayList<Object> tableEntry = table.get(i);

            double categoricalTotal = 0;
            int entryNumber = 0;
            //update entry number and the total
            for (Expense expense : categoryStats) {
                categoricalTotal += Double.parseDouble(expense.getPrice().value);
                entryNumber++;
            }

            tableEntry.set(0, categories.get(i));
            tableEntry.set(1, categoricalTotal);
            tableEntry.set(2, entryNumber);
            if (hasFourColumns) {
                tableEntry.set(3, categoricalTotal);
            }

            addToTableEntry(entryForTotal, categoricalTotal, entryNumber, hasFourColumns);
        }

        double totalAmount = (Double) entryForTotal.get(1);

        if (hasFourColumns) {
            for (int i = 0; i < categories.size() + 1; i++) {
                ArrayList<Object> tableEntry = table.get(i);
                double categoricalTotal = (Double) tableEntry.get(3);
                double roundedResult = Math.round(categoricalTotal * 10000 / totalAmount) / 100.0;
                tableEntry.set(3, roundedResult);
            }
        }

    }

    /**
     * Adds the new values to the existing values in the table entry
     */
    private void addToTableEntry(ArrayList<Object> entryForTotal,
                                 double categoricalTotal, int entryNumber, boolean hasFourColumns) {
        double oldTotal = (Double) entryForTotal.get(1);
        int oldCount = (Integer) entryForTotal.get(2);
        entryForTotal.set(1, oldTotal + categoricalTotal);
        entryForTotal.set(2, oldCount + entryNumber);
        if (hasFourColumns) {
            double updatedTotal = (Double) entryForTotal.get(1);
            entryForTotal.set(3, updatedTotal);
        }
    }

    /**
     * Prints out the table to the console such that table entry element comes after its header
     * @param table The filled table to be printed
     * @param headers The headers to label the table entries
     */
    private void convertTableToString(ArrayList<ArrayList<Object>> table, List<String> headers) {
        for (List<Object> tableEntry: table) {
            for (int i = 0; i < tableEntry.size(); i++) {
                statsBuilder.append(headers.get(i));
                statsBuilder.append(tableEntry.get(i));
                statsBuilder.append("\n");
            }
            statsBuilder.append("\n");
        }
    }

    /**
     * Extracts the expenses that are between the 2 dates
     *
     * @return A list of categorised expenses according to their categories
     */
    private ArrayList<ArrayList<Expense>> extractRelevantExpenses(Timestamp startDate, Timestamp endDate) {
        ArrayList<ArrayList<Expense>> data = new ArrayList<>();

        List<Category> categories = collateCategoryNames(expenses);
        for (int i = 0; i <= categories.size(); i++) {
            data.add(new ArrayList<>());
        }

        for (Expense expense : expenses) {
            Timestamp date = expense.getTimestamp();
            if (date.compareTo(startDate) != -1 && date.compareTo(endDate) != 1) {
                data.get(categories.size()).add(expense);
                int index = categories.indexOf(expense.getCategory());
                data.get(index).add(expense);
            }
        }
        return data;
    }

    //to be removed after custom categories are implemented
    /**
     * Returns a list of tags used among all expenses
     */
    private static List<Category> collateCategoryNames(ObservableList<Expense> expenses) {
        Set<Category> categories = new HashSet<>();
        for (Expense expense: expenses) {
            categories.add(expense.getCategory());
        }

        List<Category> result = new ArrayList<>(categories);
        return result;
    }
}
