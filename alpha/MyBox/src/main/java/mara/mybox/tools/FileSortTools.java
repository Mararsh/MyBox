package mara.mybox.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileSortTools {

    public static enum FileSortMode {
        ModifyTimeDesc, ModifyTimeAsc, CreateTimeDesc, CreateTimeAsc,
        SizeDesc, SizeAsc, NameDesc, NameAsc, FormatDesc, FormatAsc
    }

    public static FileSortMode sortMode(String mode) {
        for (FileSortMode v : FileSortMode.values()) {
            if (v.name().equals(mode) || Languages.message(v.name()).equals(mode)) {
                return v;
            }
        }
        return null;
    }

    public static void sortFiles(List<File> files, FileSortMode sortMode) {
        if (files == null || files.isEmpty() || sortMode == null) {
            return;
        }
        try {
            switch (sortMode) {
                case ModifyTimeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f2.lastModified() - f1.lastModified();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case ModifyTimeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f1.lastModified() - f2.lastModified();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case NameDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileNameTools.compareName(f2, f1);
                        }
                    });
                    break;
                case NameAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileNameTools.compareName(f1, f2);
                        }
                    });
                    break;
                case CreateTimeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long t1 = FileTools.createTime(f1.getAbsolutePath());
                            long t2 = FileTools.createTime(f2.getAbsolutePath());
                            long diff = t2 - t1;
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case CreateTimeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long t1 = FileTools.createTime(f1.getAbsolutePath());
                            long t2 = FileTools.createTime(f2.getAbsolutePath());
                            long diff = t1 - t2;
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case SizeDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f2.length() - f1.length();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case SizeAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            long diff = f1.length() - f2.length();
                            if (diff == 0) {
                                return 0;
                            } else if (diff > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                    });
                    break;
                case FormatDesc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileNameTools.ext(f2.getName()).compareTo(FileNameTools.ext(f1.getName()));
                        }
                    });
                    break;
                case FormatAsc:
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return FileNameTools.ext(f1.getName()).compareTo(FileNameTools.ext(f2.getName()));
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static List<File> sortFiles(File path, FileSortMode sortMode) {
        if (path == null || !path.isDirectory()) {
            return null;
        }
        File[] pathFiles = path.listFiles();
        if (pathFiles == null || pathFiles.length == 0) {
            return null;
        }
        List<File> files = new ArrayList<>();
        for (File file : pathFiles) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        sortFiles(files, sortMode);
        return files;
    }

    public static void sortFileInformations(List<FileInformation> files, FileSortMode sortMode) {
        if (files == null || files.isEmpty() || sortMode == null) {
            return;
        }
        switch (sortMode) {
            case ModifyTimeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f2.getFile().lastModified() - f1.getFile().lastModified());
                    }
                });
                break;
            case ModifyTimeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f1.getFile().lastModified() - f2.getFile().lastModified());
                    }
                });
                break;
            case NameDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileNameTools.compareName(f2.getFile(), f1.getFile());
                    }
                });
                break;
            case NameAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileNameTools.compareName(f1.getFile(), f2.getFile());
                    }
                });
                break;
            case CreateTimeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        long t1 = FileTools.createTime(f1.getFile().getAbsolutePath());
                        long t2 = FileTools.createTime(f2.getFile().getAbsolutePath());
                        return (int) (t2 - t1);
                    }
                });
                break;
            case CreateTimeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        long t1 = FileTools.createTime(f1.getFile().getAbsolutePath());
                        long t2 = FileTools.createTime(f2.getFile().getAbsolutePath());
                        return (int) (t1 - t2);
                    }
                });
                break;
            case SizeDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f2.getFile().length() - f1.getFile().length());
                    }
                });
                break;
            case SizeAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return (int) (f1.getFile().length() - f2.getFile().length());
                    }
                });
                break;
            case FormatDesc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileNameTools.ext(f2.getFile().getName()).compareTo(FileNameTools.ext(f1.getFile().getName()));
                    }
                });
                break;
            case FormatAsc:
                Collections.sort(files, new Comparator<FileInformation>() {
                    @Override
                    public int compare(FileInformation f1, FileInformation f2) {
                        return FileNameTools.ext(f1.getFile().getName()).compareTo(FileNameTools.ext(f2.getFile().getName()));
                    }
                });
                break;
        }
    }

}
