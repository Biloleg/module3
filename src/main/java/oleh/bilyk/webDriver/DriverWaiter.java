package oleh.bilyk.webDriver;

import oleh.bilyk.helpers.CollectionHelper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static oleh.bilyk.webDriver.Driver.getDriver;
import static oleh.bilyk.webDriver.Driver.log;

/**
 * #Summary:
 * #Author: Oleh_Bilyk
 * #Author’s Email: oleh_bilyk@epam.com
 * #Creation Date: 22/04/2020
 * #Comments:
 */
public class DriverWaiter {
    private long pollingInterval;

    //<editor-fold desc="Public methods">
    public void waitForElementPresent(final By element, final long... msToWait) {
        final long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        try {
            Waiter waiter = () ->
                    waitUntilExpected(driver -> {
                        try {
                            driver.findElement(element);
                            log.info(String.format("The element '%s' is present on the page.", element.toString()));
                            return true;
                        } catch (NoSuchElementException e) {
                            log.info(String.format("The element '%s' is not present on the page. Waiting...", element.toString()));
                            return false;
                        } catch (TimeoutException e) {
                            log.info(String.format("Timed out after '%d' millisec(s) waiting for: '%s'.", msToWaitLoc, element.toString()));
                            return false;
                        }
                    }, msToWaitLoc);
            changeTimeOutsAndWait(waiter, 50, 10);
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("The Element '%s' is not present after timeout '%d' millisec(s).", element, msToWaitLoc));
        }
    }

    public void waitForElementDisplayed(final By element, final long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        waitForElementPresent(element, msToWaitLoc);
        waitForElementDisplayed(getDriver().findElement(element), msToWaitLoc);
    }

    public void waitForElementDisplayed(final WebElement element, final long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        try {
            Waiter waiter = () -> waitUntilExpected(driver -> {
                try {
                    if (element.isDisplayed()) {
                        log.info(String.format("The element '%s' has been displayed.", element.toString()));
                        return true;
                    } else {
                        log.info(String.format("The element '%s' isn't displayed on the page. Waiting...", element.toString()));
                        return false;
                    }
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    log.info(String.format("The element '%s' has been disappeared.", element.toString()));
                    return false;
                }
            }, msToWaitLoc);
            changeTimeOutsAndWait(waiter, 50, 10);
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("The element '%s' is not displayed after timeout '%d' millisec(s).", element, msToWaitLoc));
        }
    }

    public void waitForElementIsNotDisplayed(final By element, final long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        try {
            Waiter waiter = () ->
                    waitUntilExpected(driver -> {
                        try {
                            if (!driver.findElement(element).isDisplayed()) {
                                log.info(String.format("The element '%s' has been disappeared.", element.toString()));
                                return true;
                            } else {
                                log.info(String.format("The element '%s' is still displayed on the page. Waiting...", element.toString()));
                                return false;
                            }
                        } catch (NoSuchElementException | StaleElementReferenceException e) {
                            log.info(String.format("The element '%s' has been disappeared.", element.toString()));
                            return true;
                        }
                    }, msToWaitLoc);
            changeTimeOutsAndWait(waiter, 50, 10);
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("The Element '%s' is still displayed after timeout '%d' millisec(s).", element, msToWaitLoc));
        }
    }

    public void waitForElementIsNotPresent(final By element, final long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        try {
            Waiter waiter = () ->
                    waitUntilExpected(driver -> {
                        try {
                            driver.findElement(element);
                            log.info(String.format("The element '%s' is still present on the page. Waiting...", element.toString()));
                            return false;
                        } catch (NoSuchElementException e) {
                            log.info(String.format("The element '%s' has been disappeared from DOM as expected.", element.toString()));
                            return true;
                        }
                    }, msToWaitLoc);
            changeTimeOutsAndWait(waiter, 50, 10);
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("The Element '%s' is still present after timeout '%d' millisec(s).", element, msToWaitLoc));
        }
    }

    public WebElement waitForElement(By by, long... msToWait) {
        long timeStart = System.currentTimeMillis();
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        WebElement element = null;
        try {
            element = (new WebDriverWait(getDriver(), msToWaitLoc)).
                    until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        long timeElapsed = (System.currentTimeMillis() - timeStart) / 1000;
        if (timeElapsed > 2) {
            log.info("Real waiting of: " + by + " element was more than 2 millisec(s): " + timeElapsed + " ");
        }
        return element;
    }

    public boolean waitUntilExpected(Function<WebDriver, Boolean> function, final long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 10_000;
        WebDriverWait wait = new WebDriverWait(getDriver(), msToWaitLoc / 1000);
        wait.pollingEvery(Duration.of(pollingInterval, ChronoUnit.MILLIS));
        return wait.until(function);
    }

    public void sleep(long... msToWait) {
        long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 1;
        try {
            Thread.sleep(msToWaitLoc);
        } catch (InterruptedException e) {
            log.info(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void setPollingInterval(final long... interval) {
        this.pollingInterval = interval.length > 0 ? interval[0] : 500;
    }

    public void setImplicitlyWait(final long... msToWait) {
        final long msToWaitLoc = msToWait.length > 0 ? msToWait[0] : 3_000;
        getDriver().manage().timeouts().implicitlyWait(msToWaitLoc, TimeUnit.MILLISECONDS);
    }

    public void changeTimeOutsAndWait(Waiter waiter, long implicitlyWait, long pollingInterval) {
        setImplicitlyWait(implicitlyWait);
        setPollingInterval(pollingInterval);
        waiter.applyWait();
        setImplicitlyWait();
        setPollingInterval();
    }
    //</editor-fold>
}
