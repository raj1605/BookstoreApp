package business.book;

public class Book {

	/*
	 * TODO: Create private fields corresponding to the fields in the
	 * book table of your database. Generate a constructor that
	 * uses those fields. Generate getter methods for those fields,
	 * and generate a toString method that uses those fields.
	 */

	private long bookId;
	private String title;
	private String author;
	private int price;
	private boolean isPublic;
	private long categoryId;
	private int rating;
	private String description;
	private boolean isFeatured;

	public Book(long bookId, String title, String author, String description, int price, int rating, boolean isPublic, boolean isFeatured, long categoryId) {
		this.bookId = bookId;
		this.title = title;
		this.author = author;
		this.price = price;
		this.isPublic = isPublic;
		this.categoryId = categoryId;
		this.rating = rating;
		this.description = description;
		this.isFeatured = isFeatured;
	}

	public long getBookId() {
		return bookId;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public int getPrice() {
		return price;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public long getCategoryId() {
		return categoryId;
	}

	@Override
	public String toString() {
		return "Book{" +
				"bookId=" + bookId +
				", title='" + title + '\'' +
				", author='" + author + '\'' +
				", price=" + price +
				", isPublic=" + isPublic +
				", categoryId=" + categoryId +
				", rating=" + rating +
				", description='" + description + '\'' +
				", isFeatured=" + isFeatured +
				'}';
	}

	public int getRating() {
		return rating;
	}

	public String getDescription() {
		return description;
	}

	public boolean getIsFeatured() {
		return isFeatured;
	}
}
