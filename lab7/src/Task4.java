public class Task4 {

    // 1. Вузькоспеціалізовані інтерфейси за ролями (Role-based interfaces)

    public interface FileReadable {
        byte[] downloadFile(String name);
    }

    public interface FileWritable {
        void uploadFile(String name, byte[] data);
        void deleteFile(String name);
    }

    public interface PublicShareable {
        String generatePublicShareLink(String name);
    }

    public interface Archivable {
        void compressArchive(String folderPath);
    }

    public interface Mountable {
        void mountNetworkDrive(String driveLetter);
    }

    // 2. CDN провайдер реалізує лише те, що підтримує (Читання та публічні посилання)
    public static class ReadOnlyPublicCDNProvider implements FileReadable, PublicShareable {

        @Override
        public byte[] downloadFile(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            return new byte[0];
        }

        @Override
        public String generatePublicShareLink(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            return "https://example.com/share/" + name;
        }
    }

    // 3. Локальний синхронізатор реалізує роботу з файлами та архівацію
    public static class LocalFileSyncProvider implements FileReadable, FileWritable, Archivable {

        @Override
        public byte[] downloadFile(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            return new byte[0];
        }

        @Override
        public void uploadFile(String name, byte[] data) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
            System.out.println("Uploaded " + name + " to local disk.");
        }

        @Override
        public void deleteFile(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }
            System.out.println("Deleted " + name + " from local disk.");
        }

        @Override
        public void compressArchive(String folderPath) {
            if (folderPath == null || folderPath.isBlank()) {
                throw new IllegalArgumentException("Folder path cannot be null or empty");
            }
            System.out.println("Archived " + folderPath + ".");
        }
    }
}
