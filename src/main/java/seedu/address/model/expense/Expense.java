package seedu.address.model.expense;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.model.tag.Tag;

/**
 * Represents a Expense in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Expense {

    // Identity fields
    private final Description description;
    private final Price price;
    //private final Email email;

    // Data fields
    //private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Expense(Description description, Price price, /*Email email, Address address, */Set<Tag> tags) {
        requireAllNonNull(description, price, /*email, address,*/ tags);
        this.description = description;
        this.price = price;
        //this.email = email;
        //this.address = address;
        this.tags.addAll(tags);
    }

    public Description getDescription() {
        return description;
    }

    public Price getPrice() {
        return price;
    }

    /*public Email getEmail() {
        return null;
    }

    public Address getAddress() {
        return null;
    }*/

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both expenses of the same description have at least one other identity field that is the same.
     * This defines a weaker notion of equality between two expenses.
     */
    public boolean isSameExpense(Expense otherExpense) {
        if (otherExpense == this) {
            return true;
        }

        return otherExpense != null
                && otherExpense.getDescription().equals(getDescription())
                && (otherExpense.getPrice().equals(getPrice())) /*|| otherExpense.getEmail().equals(getEmail()))*/;
    }

    /**
     * Returns true if both expenses have the same identity and data fields.
     * This defines a stronger notion of equality between two expenses.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Expense)) {
            return false;
        }

        Expense otherExpense = (Expense) other;
        return otherExpense.getDescription().equals(getDescription())
                && otherExpense.getPrice().equals(getPrice())
                /*&& otherExpense.getEmail().equals(getEmail())
                && otherExpense.getAddress().equals(getAddress())*/
                && otherExpense.getTags().equals(getTags());
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(description, price, /*email, address,*/ tags);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("|| Description: ")
                .append(getDescription())
                .append(" Price: ")
                .append(getPrice())
                /*.append(" Email: ")
                .append(getEmail())
                .append(" Address: ")
                .append(getAddress())*/
                .append(" Tags: ");
        getTags().forEach(builder::append);
        builder.append("||");
        return builder.toString();
    }

}
