package jq.m12.pipelines;

import java.time.LocalDate;

/**
 * The work one task does, such as "copy yesterday's rides into the warehouse".
 * It gets the logical date: the day of data to process, never "today".
 * Throwing any exception means this attempt failed.
 */
@FunctionalInterface
public interface TaskAction {
    void run(LocalDate logicalDate) throws Exception;
}
