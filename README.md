## Overview
This is a **standalone Java tool** that processes **Google Trends CSV exports** to compute a **Z-Score** of the latest month's search interest compared to historical data.  
It reads multiple CSV files, aggregates values by month, and outputs the result via a GUI popup.  
Designed for **statistical analysis of time-series interest data**, with clean parsing, validation, and extensible structure.

## Features

* Read and validate multiple Google Trends CSV exports from a `/Data` folder

* Aggregate search interest values by month (YYYY-MM) across all files

* Compute **population Z-Score** of the latest month:  
  `(historical_mean - current_value) / std_dev`  
  → **Positive = below-average interest**  
  → **Negative = above-average interest**

* Display result in a simple GUI dialog (`JOptionPane`)

* Handle format errors and missing data with clear messages

* Run as a **self-contained executable JAR**

## Tech Stack

* Language: Java SE (no external libraries)

* GUI: `javax.swing.JOptionPane`

* File I/O: `BufferedReader`, `File`

* Packaging: Executable JAR

* Input: Google Trends multi-time series CSV exports

## Setup and Execution

1. Clone this repository
   ```bash
   git clone https://github.com/Alvaro05RB/Z-ScoreGoogleTrends
   
2. Compile the source code
    ```bash
   javac src/ZScoreTrends.java -d out
   jar cfe zscore-trends.jar ZScoreTrends -C out
  
3. Create a runtime directory and place the compiled zscore-trends.jar there
   
4. Inside that directory, create a folder named Data
   
5. Download your Google Trends CSV files (multi-time series format) and place them in the Data folder (Required format: first line Category: All categories, empty line, then header Month,Interest)

6. Run the tool java -jar zscore-trends.jar
    ```bash
   java -jar zscore-trends.jar
  → A popup will show:
  Z-Score for latest month (YYYY-MM): X.XX
