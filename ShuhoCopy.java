package com.example.demo;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem.Info;

@Controller
public class ShuhoCopy {

	@GetMapping("/shuho-copy")
	public String shuhoCopy(Model model) throws IOException {

		// API接続情報の作成
		Reader reader = new FileReader("config.json");
		BoxConfig boxConfig = BoxConfig.readFrom(reader);
		BoxAPIConnection apiConnect =
				BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);

		// コピー実行
		String copy = copy(model, apiConnect);

		return copy;
	}

	private String copy(Model model, BoxAPIConnection apiConnect) {
		// フォルダID
		String kanri  = "135008919453";
		String menba  = "135008918253";
		String format = "135008227394";
		String testM  = "140502654394";
		String testK  = "140501874808";

		// ファイル名
		String fnM = "週報YYYYMM.xlsx";
		String fnK = "週報YYYYMM.xlsx";

		for(int i = 0; i < 2; i++) {
			// フォーマット・IDを選択
			String fn = (i == 0) ? fnM : fnK;
			String id = (i == 0) ? testM : testK;

			// フォルダの存在チェック
			BoxFolder fCheck = new BoxFolder(apiConnect, id);
			LocalDate date = LocalDate.now();
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("YYYYMM");
			String datefmt = date.format(fmt);
			String folderName = String.format("%s%s", "週報_", datefmt);
			for(Info itemInfo: fCheck) {
				String name = itemInfo.getName();
				if(name.equals(folderName)) {
					return "check";
				}
			}

			// ファルダをコピー
			BoxFolder fCopy = new BoxFolder(apiConnect, format);
			BoxFolder destination = new BoxFolder(apiConnect, id);
			fCopy.copy(destination, folderName);

			// ファイルをリネーム
			for(Info itemInfo1: fCheck) {
				String name1 = itemInfo1.getName();
				if(name1.equals(folderName)) {
					BoxFolder folder = new BoxFolder(apiConnect, itemInfo1.getID());
					for(Info itemInfo2: folder) {
						String name2 = itemInfo2.getName();
						if(name2.equals(fn)) {
							BoxFile file = new BoxFile(apiConnect, itemInfo2.getID());
							BoxFile.Info info = file.new Info();
							StringBuffer str = new StringBuffer();
							str.append("週報");
							str.append(datefmt);
							str.append(".xlsx");
							info.setName(str.toString());
							file.updateInfo(info);
						}
					}
				}
			}

		}

		return "end";

	}
}
