import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class ZScoreGoogleTrends {
    public static void main(String[] args) {
        try {
            // Get the directory containing the JAR file
            String jarPath = ZScoreGoogleTrends.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File rootDir = jarFile.getParentFile(); // Get the parent directory of the JAR
            File dataDir = new File(rootDir, "Data");

            // List all CSV files in the Data folder
            File[] csvFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (csvFiles == null || csvFiles.length == 0) {
                JOptionPane.showMessageDialog(null, "No CSV files found in the Data folder.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Map to store values by month (assuming one value per month across all files; sums if multiple)
            Map<String, Double> monthlyValues = new TreeMap<>(); // TreeMap sorts keys naturally (YYYY-MM)

            // Process each CSV file
            for (File csv : csvFiles) {
                try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
                    String line;
                    // Read and validate first line: Category: All categories
                    line = br.readLine();
                    if (line == null || !line.trim().equals("Category: All categories")) {
                        JOptionPane.showMessageDialog(null, "Invalid format in " + csv.getName() + ": Missing or incorrect category line.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    // Skip the empty line
                    line = br.readLine();
                    if (line == null || !line.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Invalid format in " + csv.getName() + ": Expected an empty line after category.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    // Read and validate header line starting with "Month,"
                    line = br.readLine();
                    if (line == null || !line.trim().toLowerCase().startsWith("month,")) {
                        JOptionPane.showMessageDialog(null, "Invalid format in " + csv.getName() + ": Missing or incorrect header line. Expected 'Month,' at the start.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    // Read data lines
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.trim().split(",");
                        if (parts.length != 2) continue;
                        String month = parts[0].trim();
                        String valStr = parts[1].trim();
                        double val;
                        if (valStr.startsWith("<")) {
                            val = 0.0; // Treat <1 as 0
                        } else {
                            try {
                                val = Double.parseDouble(valStr);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Invalid value in " + csv.getName() + " for month " + month + ": " + valStr, "Error", JOptionPane.ERROR_MESSAGE);
                                continue;
                            }
                        }
                        // Aggregate by month (sum if multiple files have same month)
                        monthlyValues.put(month, monthlyValues.getOrDefault(month, 0.0) + val);
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error reading " + csv.getName() + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (monthlyValues.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No data aggregated from CSV files.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get sorted list of months
            List<String> sortedMonths = new ArrayList<>(monthlyValues.keySet());
            Collections.sort(sortedMonths); // Already sorted by TreeMap, but ensure

            // Identify latest month and its value
            String latestMonth = sortedMonths.get(sortedMonths.size() - 1);
            double currentValue = monthlyValues.get(latestMonth);

            // Historical values: all except the latest
            List<Double> historicalValues = new ArrayList<>();
            for (int i = 0; i < sortedMonths.size() - 1; i++) {
                historicalValues.add(monthlyValues.get(sortedMonths.get(i)));
            }

            if (historicalValues.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Insufficient historical data to compute Z-score.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Calculate historical statistics
            double histSum = 0.0;
            for (double v : historicalValues) histSum += v;
            double histMean = histSum / historicalValues.size();

            double histVariance = 0.0;
            for (double v : historicalValues) histVariance += (v - histMean) * (v - histMean);
            double histStdDev = Math.sqrt(histVariance / historicalValues.size()); // Population std dev

            if (histStdDev == 0.0) {
                JOptionPane.showMessageDialog(null, "Historical standard deviation is zero; cannot compute Z-score.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Calculate inverted Z-score for the current value
            double currentZScore = (histMean - currentValue) / histStdDev;
            JOptionPane.showMessageDialog(null, "Z-Score for latest month (" + latestMonth + "): " + String.format("%.2f", currentZScore), "Z-Score Result", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}