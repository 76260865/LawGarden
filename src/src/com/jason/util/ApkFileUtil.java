package com.jason.util;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class ApkFileUtil {

    /** Already installed */
    public static final int INSTALLED = 100;

    /** Has not been installed */
    public static final int UNINSTALLED = 200;

    /** It is newer than current version */
    public static final int NEWVERSION = 300;

    public static PackageInfo getPackageInfo(Context context, String archiveFilePath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);

        if (info != null) {
            // the secret are these two lines....
            info.applicationInfo.sourceDir = archiveFilePath;
            info.applicationInfo.publicSourceDir = archiveFilePath;

            return info;
        }

        return null;
    }

    public static int checkApkFileStatuts(Context context, int versionCode, String packageName) {
        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);

            if (packageName.equals(packageInfo.packageName)) {
                // if (packageInfo.versionCode >= versionCode) {
                return INSTALLED;
                // } else {
                // return NEWVERSION;
                // }
            }
        }
        return UNINSTALLED;
    }

    public static void launchApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void installApkFile(Context context, String appPath) {
        Intent intent = new Intent();
        intent.setDataAndType(Uri.fromFile(new File(appPath)),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
