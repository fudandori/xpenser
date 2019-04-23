package application.process;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javafx.concurrent.Task;

public class MonthProcessor extends Task<Map<String, Map<String, Float>>> {

	private static final int BALANCE_COLUMN = 5;
	private static final int CONCEPT_COLUMN = 2;
	private static final int EXPENSES_COLUMN = 3;
	private static final int DATE_COLUMN = 0;

	private File file;
	private int max;
	private int current;
	private String balance;
	private String lastBalance;
	private Iterator<Row> iterator;
	
	public MonthProcessor(File file) {
		this.file = file;
	}

	@Override
	protected Map<String, Map<String, Float>> call() throws Exception {
		Map<String, Map<String, Float>> result = new HashMap<>();

		updateProgress(1, 100);

		try {

			initializeIterator(file);

			while (iterator.hasNext()) {
				Row row = iterator.next();

				balance = getCellValue(row, BALANCE_COLUMN);

				if (row.getRowNum() > 0 && !balance.equals(lastBalance)) {

					lastBalance = balance;

					String date = getCellValue(row, DATE_COLUMN).substring(0, 7) + "-01";

					if (!result.containsKey(date)) {
						result.put(date, new HashMap<>());
					}

					Map<String, Float> map = result.get(date);

					String concept = getCellValue(row, CONCEPT_COLUMN);

					Float value = Float.parseFloat(getCellValue(row, EXPENSES_COLUMN));
					Float total = map.containsKey(concept) ? value + map.get(concept) : value;
					
					map.put(concept, round(total));

					updateProgress(current, max);
				}
				
				current++;
			}
			
			result = sortByKey(result);
			
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	private static float round(float d) {
		return new BigDecimal(Float.toString(d)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
	}

	private static String getCellValue(Row row, int column) {
		String result = null;
		Cell cell = row.getCell(column);
		CellType type = cell.getCellType();

		if (type == CellType.STRING) {
			
			result = cell.getStringCellValue();
			
		} else if (type == CellType.NUMERIC) {
			
			result = DateUtil.isCellDateFormatted(cell)
					? DateUtil
							.getJavaDate(cell.getNumericCellValue())
							.toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate()
							.toString()
					: Double.toString(cell.getNumericCellValue());
		}

		return result;
	}

	private void initializeIterator(File file) throws EncryptedDocumentException, IOException {
		Sheet sheet = WorkbookFactory.create(file).getSheetAt(0);
		this.max = sheet.getPhysicalNumberOfRows();
		this.current = 1;
		iterator = sheet.rowIterator();
	}
	
	private Map<String, Map<String, Float>> sortByKey(Map<String, Map<String, Float>> input) {
		return input.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
						LinkedHashMap<String, Map<String, Float>>::new));
	}
}
