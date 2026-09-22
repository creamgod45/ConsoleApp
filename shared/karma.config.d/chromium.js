// Use the locally installed Chromium instead of any system Chrome / downloaded binary.
const chromiumBin = 'C:/Users/v_ken.wang/chromium/win64-1083080/chrome-win/chrome.exe';

process.env.CHROME_BIN = chromiumBin;
process.env.CHROMIUM_BIN = chromiumBin;

config.set({
    browsers: ['ChromiumHeadless'],
    customLaunchers: {
        ChromiumHeadless: {
            base: 'ChromeHeadless',
            flags: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage']
        }
    }
});
