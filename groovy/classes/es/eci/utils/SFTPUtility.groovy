package es.eci.utils

@Grab(group='org.apache.commons', module='commons-vfs2', version='2.0')
@Grab(group='com.jcraft', module='jsch', version='0.1.50')
@Grab(group='org.hamcrest', module='hamcrest-core', version='1.3')
@Grab(group='commons-logging', module='commons-logging', version='1.1.3')


import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException

public class SFTPUtility {
	
		/**
		 * Method to upload a file in Remote server
		 *
		 * @param hostName
		 *            HostName of the server
		 * @param username
		 *            UserName to login
		 * @param password
		 *            Password to login
		 * @param localFilePath
		 *            LocalFilePath. Should contain the entire local file path -
		 *            Directory and Filename with \\ as separator
		 * @param remoteFilePath
		 *            remoteFilePath. Should contain the entire remote file path -
		 *            Directory and Filename with / as separator
		 */
		public static void upload(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
			JSch jsch = new JSch();
			Session session = null;
			try {
				  session = jsch.getSession(username, hostName, 22);
				  session.setConfig("StrictHostKeyChecking", "no");
				  session.setPassword(password);
				  session.connect();
		
				  Channel channel = session.openChannel("sftp");
				  channel.connect();
				  ChannelSftp sftpChannel = (ChannelSftp) channel;
				  sftpChannel.put(localFilePath, remoteFilePath);
				  sftpChannel.exit();
				  session.disconnect();
			 } catch (JSchException e) {
				  e.printStackTrace();
				  throw new Exception();
			 } catch (SftpException e) {
				  e.printStackTrace();
				  throw new Exception();
			 }
		}
		
		public static boolean move(String hostName, String username, String password, String remoteSrcFilePath, String remoteDestFilePath){
			StandardFileSystemManager manager = new StandardFileSystemManager();
	
			try {
				manager.init();
	
				// Create remote object
				FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteSrcFilePath), createDefaultOptions());
				FileObject remoteDestFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteDestFilePath), createDefaultOptions());
	
				if (remoteFile.exists()) {
					remoteFile.moveTo(remoteDestFile);;
					println("Move remote file success");
					return true;
				}
				else{
					println("Source file doesn't exist");
					return false;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}
		}
	
		/**
		 * Method to download the file from remote server location
		 *
		 * @param hostName
		 *            HostName of the server
		 * @param username
		 *            UserName to login
		 * @param password
		 *            Password to login
		 * @param localFilePath
		 *            LocalFilePath. Should contain the entire local file path -
		 *            Directory and Filename with \\ as separator
		 * @param remoteFilePath
		 *            remoteFilePath. Should contain the entire remote file path -
		 *            Directory and Filename with / as separator
		 */
		public static void download(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
	
			StandardFileSystemManager manager = new StandardFileSystemManager();
	
			try {
				manager.init();
	
				// Append _downlaod_from_sftp to the given file name.
				//String downloadFilePath = localFilePath.substring(0, localFilePath.lastIndexOf(".")) + "_downlaod_from_sftp" + localFilePath.substring(localFilePath.lastIndexOf("."), localFilePath.length());
	
				// Create local file object. Change location if necessary for new downloadFilePath
				FileObject localFile = manager.resolveFile(localFilePath);
	
				// Create remote file object
				FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
	
				// Copy local file to sftp server
				localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
	
				println("File download success");
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}
		}
	
		/**
		 * Method to delete the specified file from the remote system
		 *
		 * @param hostName
		 *            HostName of the server
		 * @param username
		 *            UserName to login
		 * @param password
		 *            Password to login
		 * @param localFilePath
		 *            LocalFilePath. Should contain the entire local file path -
		 *            Directory and Filename with \\ as separator
		 * @param remoteFilePath
		 *            remoteFilePath. Should contain the entire remote file path -
		 *            Directory and Filename with / as separator
		 */
		public static void delete(String hostName, String username, String password, String remoteFilePath) {
			StandardFileSystemManager manager = new StandardFileSystemManager();
	
			try {
				manager.init();
	
				// Create remote object
				FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
	
				if (remoteFile.exists()) {
					remoteFile.delete();
					println("Delete remote file success");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}
		}
	
		// Check remote file is exist function:
		/**
		 * Method to check if the remote file exists in the specified remote
		 * location
		 *
		 * @param hostName
		 *            HostName of the server
		 * @param username
		 *            UserName to login
		 * @param password
		 *            Password to login
		 * @param remoteFilePath
		 *            remoteFilePath. Should contain the entire remote file path -
		 *            Directory and Filename with / as separator
		 * @return Returns if the file exists in the specified remote location
		 */
		public static boolean exist(String hostName, String username, String password, String remoteFilePath) {
			StandardFileSystemManager manager = new StandardFileSystemManager();
	
			try {
				manager.init();
	
				// Create remote object
				FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
	
				println("File exist: " + remoteFile.exists());
	
				return remoteFile.exists();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}
		}
	
		/**
		 * Generates SFTP URL connection String
		 *
		 * @param hostName
		 *            HostName of the server
		 * @param username
		 *            UserName to login
		 * @param password
		 *            Password to login
		 * @param remoteFilePath
		 *            remoteFilePath. Should contain the entire remote file path -
		 *            Directory and Filename with / as separator
		 * @return concatenated SFTP URL string
		 */
		public static String createConnectionString(String hostName, String username, String password, String remoteFilePath) {
			return "sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath;
		}
	
		/**
		 * Method to setup default SFTP config
		 *
		 * @return the FileSystemOptions object containing the specified
		 *         configuration options
		 * @throws FileSystemException
		 */
		public static FileSystemOptions createDefaultOptions() throws FileSystemException {
			// Create SFTP options
			FileSystemOptions opts = new FileSystemOptions();
	
			// SSH Key checking
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
	
			/*
			 * Using the following line will cause VFS to choose File System's Root
			 * as VFS's root. If I wanted to use User's home as VFS's root then set
			 * 2nd method parameter to "true"
			 */
			// Root directory set to user home
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
	
			// Timeout is count by Milliseconds
			SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
	
			return opts;
		}
	}
