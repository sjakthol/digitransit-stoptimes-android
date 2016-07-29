package io.github.sjakthol.stoptimes.utils;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FallibleAsyncTaskTest {
    @Test
    public void test_success() {
        final CountDownLatch latch = new CountDownLatch(1);

        new FallibleAsyncTask<Integer, Integer>() {
            @Override
            public Integer runTask(Integer... params) {
                return params[0] + 1;
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Integer> res) {
                super.onPostExecute(res);

                assertThat("task successful", res.isSuccess(), is(true));
                assertThat("result correct", res.getResult(), is(13));
                latch.countDown();
            }
        }.execute(12);
    }

    @Test
    public void test_failure() {
        final CountDownLatch latch = new CountDownLatch(1);

        class TestException extends RuntimeException {}

        new FallibleAsyncTask<Integer, Integer>() {
            @Override
            public Integer runTask(Integer... params) {
                throw new TestException();
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Integer> res) {
                super.onPostExecute(res);

                assertThat("task not successful", res.isSuccess(), is(false));
                assertThat("result null", res.getResult(), is(not(notNullValue())));
                assertThat("error correct", res.getError() instanceof TestException, is(true));
                latch.countDown();
            }
        }.execute(12);
    }
}