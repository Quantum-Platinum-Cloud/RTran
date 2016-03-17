/*
 * Copyright (c) 2016 eBay Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rtran.maven

import java.io.File

import org.rtran.maven.util.MavenModelUtil
import MavenModelUtil._
import com.typesafe.scalalogging.LazyLogging
import org.rtran.api.{IProjectCtx, IRule, IRuleConfig}


class MavenAddDependenciesRule(ruleConfig: MavenAddDependenciesRuleConfig)
  extends IRule[MultiModuleMavenModel] with LazyLogging {

  override def transform(model: MultiModuleMavenModel): MultiModuleMavenModel = {
    var changes = Set.empty[File]
    model.modules filter { module =>
      ruleConfig.packageTypes match {
        case Some(set) => set contains module.pomModel.getPackaging
        case None => true
      }
    } foreach { module =>
      ruleConfig.dependencies filterNot {dep =>
        module.resolvedDependencies.exists(_.key == dep.key)
      } foreach {dep =>
        logger.info("{} added dependency {} to {}", id, dep, module.pomFile)
        module.pomModel.addDependency(dep)
        changes += module.pomFile
      }
    }
    logger.info("Rule {} was applied to {} files", id, changes.size.toString)
    model
  }

  override def isEligibleFor(projectCtx: IProjectCtx) = projectCtx.isInstanceOf[MavenProjectCtx]
}

case class MavenAddDependenciesRuleConfig(dependencies: Set[SimpleDependency],
                                          packageTypes: Option[Set[String]] = None) extends IRuleConfig