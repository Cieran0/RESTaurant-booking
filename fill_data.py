import sqlite3
import random

# Connect to the SQLite database (or create it if it doesn't exist)
conn = sqlite3.connect('bookings.db')
cursor = conn.cursor()

# Create the Tables table if it doesn't exist
cursor.execute('''
CREATE TABLE IF NOT EXISTS Tables (
    TableNumber INTEGER PRIMARY KEY,
    Seats INTEGER
)
''')

# Function to generate sequential table data with weighted seats
def generate_table_data(table_number):
    # Weighted probabilities: 4 is most common, 6 and 2 are less common, 8 is rare
    seats = random.choices(
        [2, 4, 6, 8],  # Possible seat values
        weights=[20, 50, 20, 10],  # Corresponding weights (probabilities)
        k=1  # Select 1 value
    )[0]  # Extract the selected value from the list
    return (table_number, seats)

# Insert 25 rows into the Tables table with sequential TableNumber and weighted Seats
for table_number in range(1, 27):  # Starts at 1 and goes up to 25
    table_data = generate_table_data(table_number)
    cursor.execute('''
    INSERT INTO Tables (TableNumber, Seats)
    VALUES (?, ?)
    ''', table_data)

# Commit the transaction
conn.commit()

# Close the connection
conn.close()

print("25 rows have been inserted into the Tables table with sequential TableNumbers and weighted Seats.")