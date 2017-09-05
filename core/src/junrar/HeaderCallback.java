package junrar;

import junrar.rarfile.FileHeader;

public interface HeaderCallback {

    void onFileHeader(FileHeader fileHeader);
}
