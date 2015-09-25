package org.ednovo.gooru.controllers.v1.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ednovo.gooru.application.util.GooruImageUtil;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.RequestMappingUri;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = { RequestMappingUri.V1_CROP })
@Controller
public class CropRestController extends BaseController implements ConstantProperties, ParameterProperties {

	private final String X = "x";

	private final String Y = "y";

	@Autowired
	private GooruImageUtil gooruImageUtil;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_UPDATE })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(method = RequestMethod.GET)
	public void cropImage(@RequestParam(value = MEDIA_FILE_NAME) final String filename, @RequestParam(value = WIDTH) int width, @RequestParam(value = HEIGHT) int height, @RequestParam(value = X) int x, @RequestParam(value = Y) int y, final HttpServletRequest request, final HttpServletResponse response) {
		getGooruImageUtil().cropImage(response, filename, width, height, x, y);
	}

	public GooruImageUtil getGooruImageUtil() {
		return gooruImageUtil;
	}
}
