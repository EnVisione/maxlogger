package com.enviouse.maxlogger.logging;

import com.enviouse.maxlogger.data.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SessionManager sessionManager;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final long rotateBytes;

    public LogWriter(SessionManager sessionManager, long rotateBytes) {
        this.sessionManager = sessionManager;
        this.rotateBytes = rotateBytes;
        Thread writerThread = new Thread(this::runWriter, "MaxLogger-LogWriter-" + sessionManager.getSessionFile().getFileName());
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public void log(String line) {
        String ts = LocalDateTime.now().format(FORMATTER);
        queue.offer("[" + ts + "] " + line + System.lineSeparator());
    }

    private void runWriter() {
        while (true) {
            try {
                String line = queue.take();
                writeLine(line);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void writeLine(String line) {
        try (FileChannel channel = FileChannel.open(sessionManager.getSessionFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            channel.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
            if (rotateBytes > 0 && channel.size() >= rotateBytes) {
                sessionManager.rotate();
            }
        } catch (IOException e) {
            LOGGER.error("Failed writing log line", e);
        }
    }

    public String getSessionFileName() {
        return sessionManager.getSessionFile().getFileName().toString();
    }
}
