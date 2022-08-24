package com.mmc.bpm.client.cases.definition;

import com.mmc.bpm.client.cases.definition.hook.archive.BeforeCaseArchiveHook;
import com.mmc.bpm.client.cases.definition.hook.archive.PostCaseArchiveHook;
import com.mmc.bpm.client.cases.definition.hook.assign.BeforeCaseAssignHook;
import com.mmc.bpm.client.cases.definition.hook.assign.PostCaseAssignHook;
import com.mmc.bpm.client.cases.definition.hook.close.BeforeCaseCloseHook;
import com.mmc.bpm.client.cases.definition.hook.close.PostCaseCloseHook;
import com.mmc.bpm.client.cases.definition.hook.create.BeforeCaseCreateHook;
import com.mmc.bpm.client.cases.definition.hook.create.PostCaseCreateHook;
import com.mmc.bpm.client.cases.definition.hook.update.BeforeCaseUpdateHook;
import com.mmc.bpm.client.cases.definition.hook.update.PostCaseUpdateHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CaseDefinition {

	private String id;

	private String name;

	@Builder.Default
	private BeforeCaseCreateHook beforeCaseCreateHook = new BeforeCaseCreateHook();
	@Builder.Default
	private PostCaseCreateHook postCaseCreateHook = new PostCaseCreateHook();

	@Builder.Default
	private BeforeCaseCloseHook beforeCaseCloseHook = new BeforeCaseCloseHook();
	@Builder.Default
	private PostCaseCloseHook postCaseCloseHook = new PostCaseCloseHook();

	@Builder.Default
	private BeforeCaseUpdateHook beforeCaseUpdateHook = new BeforeCaseUpdateHook();
	@Builder.Default
	private PostCaseUpdateHook postCaseUpdateHook = new PostCaseUpdateHook();

	@Builder.Default
	private BeforeCaseUpdateHook beforeCaseStateUpdateHook = new BeforeCaseUpdateHook();
	@Builder.Default
	private PostCaseUpdateHook postCaseStateUpdateHook = new PostCaseUpdateHook();

	@Builder.Default
	private BeforeCaseArchiveHook beforeCaseArchiveHook = new BeforeCaseArchiveHook();
	@Builder.Default
	private PostCaseArchiveHook postCaseArchiveHook = new PostCaseArchiveHook();

	@Builder.Default
	private BeforeCaseAssignHook beforeCaseAssignHook = new BeforeCaseAssignHook();
	@Builder.Default
	private PostCaseAssignHook postCaseAssignHook = new PostCaseAssignHook();
}
