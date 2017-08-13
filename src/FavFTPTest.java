import junit.framework.TestCase;

public class FavFTPTest extends TestCase {

    /**
     * 上传文件（可供Action/Controller层使用）
     * @param hostname FTP服务器地址
     * @param port FTP服务器端口号
     * @param username FTP登录帐号
     * @param password FTP登录密码
     * @param pathname FTP服务器保存目录
     * @param fileName 下载FTP服务器后的文件名称
     * @param inputStream 输入文件流
     * @return
     */

    public void testFavFTPUtil(){
        String hostname = "120.92.149.65";
        int port = 21;
        String username = "user";
        String password = "123456";
        String pathname = "/采集文件/BACKUP";
        String filename = "验收报告详情";
        /*String originfilename = "C:\\Users\\Downloads\\Downloads.rar";
        FavFTPUtil.uploadFileFromProduction(hostname, port, username, password, pathname, filename, originfilename);*/
        String localpath = "D:/";
        FavFTPUtil.downloadFile(hostname, port, username, password, pathname, filename, localpath);
    }

}