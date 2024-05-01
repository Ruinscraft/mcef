package com.cinemamod.mcef;

public final class MIMEUtil {
    public static String mimeFromExtension(String ext) {
        // TODO: might want to port https://github.com/CinemaMod/mcef/blob/master-1.19.2/src/main/resources/assets/mcef/mime.types
        //       adding cases to the switch isn't the most convenient thing
//        ext = ext.toLowerCase(Locale.US);
//        String ret = mimeTypeMap.get(ext);
//        if (ret != null)
//            return ret;

        // If the mimeTypeMap couldn't be loaded, fall back to common things
        return switch (ext) {
            case "htm", "html" -> "text/html";
            case "css" -> "text/css";
            case "pdf" -> "application/pdf";
            case "xz" -> "application/x-xz";
            case "tar" -> "application/x-tar";
            case "cpio" -> "application/x-cpio";
            case "7z" -> "application/x-7z-compressed";
            case "zip" -> "application/zip";
            case "js" -> "text/javascript";
            case "json" -> "application/json";
            case "jsonml" -> "application/jsonml+json";
            case "jar" -> "application/java-archive";
            case "ser" -> "application/java-serialized-object";
            case "class" -> "application/java-vm";
            case "wad" -> "application/x-doom";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "xml" -> "text/xml";
            case "txt" -> "text/plain";
            case "oga", "ogg", "spx" -> "audio/ogg";
            case "mp4", "mp4v", "mpg4" -> "video/mp4";
            case "m4a", "mp4a" -> "audio/mp4";
            case "mid", "midi", "kar", "rmi" -> "audio/midi";
            case "mpga", "mp2", "mp2a", "mp3", "mp3a", "m2a" -> "audio/mpeg";
            case "mpeg", "mpg", "mpe", "m1v", "m2v" -> "video/mpeg";
            case "jpgv" -> "video/jpeg";
            case "h264" -> "video/h264";
            case "h261" -> "video/h261";
            case "h263" -> "video/h263";
            case "webm" -> "video/webm";
            case "flv" -> "video/flv";
            case "m4v" -> "video/m4v";
            case "qt", "mov" -> "video/quicktime";
            case "ogv" -> "video/ogg";
            default -> null;
        };
    }
}
