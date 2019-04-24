package application.process;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public class SingleProcessor extends Processor<Map<String, Float>> {
	
	public SingleProcessor(File file) {
		super(file);
	}

	@Override
	protected Map<String, Float> call() throws Exception {
		
		Map<String, Float> result = new HashMap<>();
		
			initializeIterator(file);
			
			while (iterator.hasNext()) {
				Row row = iterator.next();

				map(row, result);
				
				updateProgress(current++, max);
			}

		return result;
	}
}
