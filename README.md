
This is the DownloaderOne. Easy to work with media downloader.

Using JavaFX, youtube-dl and
[Sapher Youtubedl-Java](https://github.com/sapher/youtubedl-java/ "Named link title") open source software.

Work in progress...

WANT

- LOGGER. If error, read error again after popup is gone/Log file system
- Settings MetaTags in file with Apache Tika
- Settings: Make possible to set different audio/video formats

LIKE
- Make Tab Play to play the downloads (or as play action in row)
- No scrollbar horizontal and vertical when resizing
- Get download url from task when downloading (info panel/dialog.modal)
- Translations

RESOLVED
- Playlist split If "?list=SDSGDGSD" then split it in different downloadTasks (youtube specific). Use youtube-dl command (example) 'https://www.youtube.com/watch?v=GpMoRS_9bcM&list=PLrO4SwMB0WVNM9d6bOjHJLZFTux5UuqnL' --flat-playlist --dump-single-json"
- Make editable datalist initial websites/Combobox is editable
- Duplicate id nr in table when restarting tasks check id before restart
- Total size downloadButton in row (Read youtubeResponse?)
- Check tor proces exists before start
- Make settings screen (paths edit)
- Progress in percentage number
- About dialog with changes text
- Show Tor enabled on Download page
- If list: make input urls bigger, resize and when finished make smaller
- Make Error message smaller when shown
- Initial row -> remove -100% (change visibility?)
- Initial row -> set status "Searching" before "Downloading"
- Reference use as Folder name where download is saved
- Get Version Youtube-dl
- Implement new STATE "Converting" / Split up downloading and conversion in two different youtube-dl commands to let this work?
- Start first download directly when multiple downloads are waiting
- Set output titles names ext etc..
- Show error message checkbox not working

