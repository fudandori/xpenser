package application.process;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;

public class MonthProcessor extends Processor<Map<String, Map<String, Float>>> {

	public MonthProcessor(File file) {
		super(file);
	}

	@Override
	protected Map<String, Map<String, Float>> call() throws Exception {

		Map<String, Map<String, Float>> result = new HashMap<>();

		initializeIterator(file);

		while (iterator.hasNext()) {

			Row row = iterator.next();

			map(row, getMap(row, result));

			updateProgress(current++, max);
		}

		return sortByKey(result);
	}

	private Map<String, Float> getMap(Row row, Map<String, Map<String, Float>> source) {
		Map<String, Float> result = null;

		if (row.getRowNum() > 0) {

			String date = getCellValue(row, DATE_COLUMN).substring(0, 7) + "-01";

			if (!source.containsKey(date)) {
				source.put(date, new HashMap<>());
			}

			result = source.get(date);
		}

		return result;
	}

	private Map<String, Map<String, Float>> sortByKey(Map<String, Map<String, Float>> input) {
		return input.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
						LinkedHashMap<String, Map<String, Float>>::new));
	}
}
