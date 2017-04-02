import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileUtil
{
	private static boolean hasEnding(File file, String ending)
	{
		return file.getName().toLowerCase().endsWith(ending);
	}

	static void walk(String path, FileVisitor fileVisitor) throws IOException, URISyntaxException
	{
		File file = new File(path).getCanonicalFile();

		if (file.getParentFile().getName().equals(".git")) {
			return;
		}

		if (!file.isDirectory()) {
			if (hasEnding(file, ".java") || hasEnding(file, ".c") || hasEnding(file, ".h") || hasEnding(file, ".cpp") ||
					hasEnding(file, ".hpp")) {
				fileVisitor.visit(file);
				return;
			}
		}

		File[] list = file.listFiles();
		if (list == null)
			return;

		for (File f : list) {
			walk(f.getAbsolutePath(), fileVisitor);
		}
	}

	@FunctionalInterface
	public interface FileVisitor {
		void visit(File file) throws IOException, URISyntaxException;
	}

	static String readFile(String file) throws IOException
	{
		return String.join("\n", Files.readAllLines(Paths.get(file)));
	}

	private static void writeFile(String file, String content, OpenOption... option)
	{
		try {
			Files.write(Paths.get(file), content.getBytes(), option);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void createFile(String file, String content)
	{
		writeFile(file, content);
	}

	static void copyFile(String from, String to) throws IOException
	{
		Path fromPath = Paths.get(from);
		Path toPath = Paths.get(to);
		if (Files.exists(toPath)) {
			Files.delete(toPath);
		}
		Files.copy(fromPath, toPath);
	}
}
